package org.graphstream.stream.file.gml;

import java.io.IOException;
import java.util.HashMap;

import org.graphstream.graph.implementations.AbstractElement.AttributeChangeEvent;
import org.graphstream.stream.SourceBase.ElementType;
import org.graphstream.stream.file.FileSourceGML;

public class GMLContext {
	FileSourceGML gml;
	//GMLParser parser;
	String sourceId;
	boolean directed;
	protected KeyValues nextStep = null;
	boolean inGraph = false;

	GMLContext(FileSourceGML gml) {
		this.gml = gml;
		this.sourceId = String.format("<GML stream %d>",
				System.currentTimeMillis());
	}

	void handleKeyValues(KeyValues kv) throws IOException {
		if (nextStep != null) {
			insertKeyValues(nextStep);
			nextStep = null;
		}

		try {
			if (kv != null) {
				insertKeyValues(kv);
			}
		} catch (IOException e) {
			throw new IOException(e);
		}
	}

	void setNextStep(KeyValues kv) {
		nextStep = kv;
	}

	public void setDirected(boolean on) {
		directed = on;
	}
	
	void setIsInGraph(boolean on) {
		inGraph = on;
	}

	public void addNodeOrEdge(String element, KeyValues kv) {
		System.err.printf("adding %s %n", element);
	}

	protected void insertKeyValues(KeyValues kv) throws IOException {
		if (kv.key != null) {
			if (inGraph) {
				if (kv.key.equals("node") || kv.key.equals("add-node")) {
					handleAddNode(kv);
				} else if (kv.key.equals("edge") || kv.key.equals("add-edge")) {
					handleAddEdge(kv);
				} else if (kv.key.equals("del-node") || kv.key.equals("-node")) {
					handleDelNode(kv);
				} else if (kv.key.equals("del-edge") || kv.key.equals("-edge")) {
					handleDelEdge(kv);
				} else if (kv.key.equals("change-node")
						|| kv.key.equals("+node")) {
					handleChangeNode(kv);
				} else if (kv.key.equals("change-edge")
						|| kv.key.equals("+edge")) {
					handleChangeEdge(kv);
				} else if (kv.key.equals("step")) {
					handleStep(kv);
				} else if (kv.key.equals("directed")) {
					setDirected(getBoolean((String) kv.get("directed")));
				} else {
					if (kv.key.startsWith("-")) {
						gml.sendAttributeChangedEvent(sourceId, sourceId,
								ElementType.GRAPH, kv.key.substring(1),
								AttributeChangeEvent.REMOVE, null, null);
					} else {
						gml.sendAttributeChangedEvent(sourceId, sourceId,
								ElementType.GRAPH, kv.key,
								AttributeChangeEvent.ADD, null,
								compositeAttribute(kv));
					}
				}
			} else {
				// XXX Should we consider these events pertain to the graph ?
				// XXX

				if (kv.key.startsWith("-")) {
					gml.sendAttributeChangedEvent(sourceId, sourceId,
							ElementType.GRAPH, kv.key.substring(1),
							AttributeChangeEvent.REMOVE, null, null);
				} else {
					gml.sendAttributeChangedEvent(sourceId, sourceId,
							ElementType.GRAPH, kv.key,
							AttributeChangeEvent.ADD, null,
							compositeAttribute(kv));
				}
			}
		}
	}

	protected Object compositeAttribute(KeyValues kv) {
		if (kv.size() < 2) {
			return kv.get(kv.key);
		} else {
			return kv;
		}
	}

	protected void handleAddNode(KeyValues kv) throws IOException {
		Object thing = kv.get("node");
		if (thing == null)
			thing = kv.get("add-node");
		if (thing == null)
			kv.error("expecting a node or add-node token here");

		if (thing instanceof String) {
			String id = (String) thing;
			gml.sendNodeAdded(sourceId, id);
		} else if (thing instanceof KeyValues) {
			KeyValues node = (KeyValues) thing;
			String id = node.reqString("id");

			gml.sendNodeAdded(sourceId, id);
			handleNodeAttributes(id, node);
		} else {
			kv.error("unknown token type");
		}
	}

	protected long edgeid = 0;

	protected void handleAddEdge(KeyValues kv) throws IOException {
		Object thing = kv.get("edge");
		if (thing == null)
			thing = kv.get("add-edge");
		if (thing == null)
			kv.error("expecting a edge or add-edge token here");
		if (!(thing instanceof KeyValues))
			kv.error("expecting a set of values for the new edge");

		KeyValues edge = (KeyValues) thing;
		String id = edge.optString("id");

		String src = edge.reqString("source");
		String trg = edge.reqString("target");

		if (id == null)
			id = String.format("%s_%s_%d", src, trg, edgeid++);

		String dir = edge.optString("directed");

		boolean directed = this.directed;

		if (dir != null) {
			directed = getBoolean(dir);
		}

		gml.sendEdgeAdded(sourceId, id, src, trg, directed);

		handleEdgeAttributes(id, edge);
	}

	protected void handleDelNode(KeyValues kv) throws IOException {
		Object thing = kv.get("del-node");
		if (thing == null)
			thing = kv.get("-node");
		if (thing == null)
			kv.error("expecting a del-node or -node token here");

		if (thing instanceof String) {
			String id = (String) thing;
			gml.sendNodeRemoved(sourceId, id);
		} else if (thing instanceof KeyValues) {
			KeyValues node = (KeyValues) thing;
			String id = node.reqString("id");
			gml.sendNodeRemoved(sourceId, id);
		} else {
			kv.error("unknown token type");
		}
	}

	protected void handleDelEdge(KeyValues kv) throws IOException {
		Object thing = kv.get("del-edge");
		if (thing == null)
			thing = kv.get("-edge");
		if (thing == null)
			kv.error("expecting a del-edge or -edge token here");

		if (thing instanceof String) {
			String id = (String) thing;
			gml.sendEdgeRemoved(sourceId, id);
		} else if (thing instanceof KeyValues) {
			KeyValues edge = (KeyValues) thing;
			String id = edge.reqString("id");
			gml.sendEdgeRemoved(sourceId, id);
		} else {
			kv.error("unknown token type");
		}
	}

	protected void handleChangeNode(KeyValues kv) throws IOException {
		Object thing = kv.get("change-node");
		if (thing == null)
			thing = kv.get("+node");
		if (thing == null)
			kv.error("expecting a change-node or +node token here");
		if (!(thing instanceof KeyValues))
			kv.error("expecting a set of values");

		KeyValues node = (KeyValues) thing;
		String id = node.reqString("id");

		handleNodeAttributes(id, node);
	}

	protected void handleChangeEdge(KeyValues kv) throws IOException {
		Object thing = kv.get("change-edge");
		if (thing == null)
			thing = kv.get("+edge");
		if (thing == null)
			kv.error("expecting a change-edge or +edge token here");
		if (!(thing instanceof KeyValues))
			kv.error("expecting a set of values");

		KeyValues edge = (KeyValues) thing;
		String id = edge.reqString("id");

		handleEdgeAttributes(id, edge);
	}

	protected void handleNodeAttributes(String id, KeyValues node) {
		for (String key : node.keySet()) {
			if (key.startsWith("-")) {
				if (key.equals("-label"))
					key = "-ui.label";

				gml.sendAttributeChangedEvent(sourceId, id, ElementType.NODE,
						key.substring(1), AttributeChangeEvent.REMOVE, null,
						null);
			} else {
				if (key.equals("graphics")
						&& node.get("graphics") instanceof KeyValues) {
					Graphics graphics = optNodeStyle((KeyValues) node
							.get("graphics"));

					if (graphics != null) {
						if (graphics.position != null) {
							gml.sendAttributeChangedEvent(sourceId, id,
									ElementType.NODE, "xyz",
									AttributeChangeEvent.ADD, null,
									graphics.getPosition());
						}
						if (graphics.style != null) {
							gml.sendAttributeChangedEvent(sourceId, id,
									ElementType.NODE, "ui.style",
									AttributeChangeEvent.ADD, null,
									graphics.style);
						}
					}
				} else {
					String k = key;

					if (key.equals("label"))
						k = "ui.label";

					gml.sendAttributeChangedEvent(sourceId, id,
							ElementType.NODE, k, AttributeChangeEvent.ADD,
							null, node.get(key));
				}
			}
		}
	}

	protected void handleEdgeAttributes(String id, KeyValues edge) {
		for (String key : edge.keySet()) {
			if (key.startsWith("-")) {
				if (key.equals("-label"))
					key = "-ui.label";

				gml.sendAttributeChangedEvent(sourceId, id, ElementType.EDGE,
						key.substring(1), AttributeChangeEvent.REMOVE, null,
						null);
			} else {
				if (key.equals("graphics")
						&& edge.get("graphics") instanceof KeyValues) {
					Graphics graphics = optEdgeStyle((KeyValues) edge
							.get("graphics"));

					if (graphics != null) {
						if (graphics.style != null) {
							gml.sendAttributeChangedEvent(sourceId, id,
									ElementType.EDGE, "ui.style",
									AttributeChangeEvent.ADD, null,
									graphics.style);
						}
					}
				} else {
					String k = key;

					if (key.equals("label"))
						k = "ui.label";

					gml.sendAttributeChangedEvent(sourceId, id,
							ElementType.EDGE, k, AttributeChangeEvent.ADD,
							null, edge.get(key));
				}
			}
		}
	}

	protected void handleStep(KeyValues kv) throws IOException {
		gml.sendStepBegins(sourceId, kv.reqNumber("step"));
	}

	protected Graphics optNodeStyle(KeyValues kv) {
		Graphics graphics = null;

		if (kv != null) {
			StringBuffer style = new StringBuffer();
			String w = null, h = null, d = null;
			graphics = new Graphics();

			if (kv.get("x") != null) {
				graphics.setX(asDouble((String) kv.get("x")));
			}
			if (kv.get("y") != null) {
				graphics.setY(asDouble((String) kv.get("y")));
			}
			if (kv.get("z") != null) {
				graphics.setZ(asDouble((String) kv.get("z")));
			}
			if (kv.get("w") != null) {
				w = (String) kv.get("w");
			}
			if (kv.get("h") != null) {
				h = (String) kv.get("h");
			}
			if (kv.get("d") != null) {
				d = (String) kv.get("d");
			}
			if (w != null || h != null || d != null) {
				int ww = w != null ? (int) asDouble(w) : 0;
				int hh = h != null ? (int) asDouble(h) : 0;
				int dd = d != null ? (int) asDouble(d) : 0;
				style.append(String.format("size: %dpx, %dpx, %dpx; ", ww, hh,
						dd));
			}
			if (kv.get("type") != null) {
				style.append(String.format("shape: %s; ",
						asNodeShape((String) kv.get("type"))));
			}

			commonGraphicsAttributes(kv, style);
			graphics.style = style.toString();
		}

		return graphics;
	}

	protected Graphics optEdgeStyle(KeyValues kv) {
		Graphics graphics = null;

		if (kv != null) {
			StringBuffer style = new StringBuffer();
			String w = null;
			graphics = new Graphics();

			if (kv.get("width") != null) {
				w = (String) kv.get("width");
			} else if (kv.get("w") != null) {
				w = (String) kv.get("w");
			}
			if (w != null) {
				double ww = w != null ? asDouble(w) : 0.0;
				style.append(String.format("size: %fpx;", ww));
			}
			if (kv.get("type") != null) {
				style.append(String.format("shape: %s; ",
						asEdgeShape((String) kv.get("type"))));
			}

			commonGraphicsAttributes(kv, style);
			graphics.style = style.toString();
		}

		return graphics;
	}

	protected void commonGraphicsAttributes(KeyValues kv, StringBuffer style) {
		if (kv.get("fill") != null) {
			style.append(String.format("fill-color: %s; ", kv.get("fill")));
		}
		if (kv.get("outline") != null) {
			style.append(String.format("stroke-color: %s; ", kv.get("outline")));
		}
		if (kv.get("outline_width") != null) {
			style.append(String.format("stroke-width: %spx; ",
					kv.get("outline_width")));
		}
		if ((kv.get("outline") != null) || (kv.get("outline_width") != null)) {
			style.append("stroke-mode: plain; ");
		}
		if (kv.get("anchor") != null) {
			style.append(String.format("text-alginment: %s; ",
					asTextAlignment((String) kv.get("anchor"))));
		}
		if (kv.get("image") != null) {
			style.append(String.format("icon-mode: at-left; icon: %s; ",
					(String) kv.get("image")));
		}
		if (kv.get("arrow") != null) {
			style.append(String.format("arrow-shape: %s; ",
					asArrowShape((String) kv.get("arrow"))));
		}
		if (kv.get("font") != null) {
			style.append(String.format("font: %s; ", (String) kv.get("font")));
		}
	}

	protected double asDouble(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	protected String asNodeShape(String type) {
		if (type.equals("ellipse") || type.equals("oval")) {
			return "circle";
		} else if (type.equals("rectangle") || type.equals("box")) {
			return "box";
		} else if (type.equals("rounded-box")) {
			return "rounded-box";
		} else if (type.equals("cross")) {
			return "cross";
		} else if (type.equals("freeplane")) {
			return "freeplane";
		} else if (type.equals("losange") || type.equals("diamond")) {
			return "diamond";
		} else {
			return "circle";
		}
	}

	protected String asEdgeShape(String type) {
		if (type.equals("line")) {
			return "line";
		} else if (type.equals("cubic-curve")) {
			return "cubic-curve";
		} else if (type.equals("angle")) {
			return "angle";
		} else if (type.equals("blob")) {
			return "blob";
		} else if (type.equals("freeplane")) {
			return "freeplane";
		} else {
			return "line";
		}
	}

	protected String asTextAlignment(String anchor) {
		if (anchor.equals("c")) {
			return "center";
		} else if (anchor.equals("n")) {
			return "above";
		} else if (anchor.equals("ne")) {
			return "at-right";
		} else if (anchor.equals("e")) {
			return "at-right";
		} else if (anchor.equals("se")) {
			return "at-right";
		} else if (anchor.equals("s")) {
			return "under";
		} else if (anchor.equals("sw")) {
			return "at-left";
		} else if (anchor.equals("w")) {
			return "at-left";
		} else if (anchor.equals("nw")) {
			return "at-left";
		} else {
			return "center";
		}
	}

	protected String asArrowShape(String arrow) {
		if (arrow.equals("none")) {
			return "none";
		} else if (arrow.equals("last")) {
			return "arrow";
		} else {
			return "none";
		}
	}

	protected boolean getBoolean(String bool) {
		if (bool.equals("1") || bool.equals("true") || bool.equals("yes")
				|| bool.equals("y"))
			return true;

		return false;
	}
}

class Graphics {
	public double[] position = null;
	public String style = null;

	public void setX(double value) {
		if (position == null)
			position = new double[3];

		position[0] = value;
	}

	public void setY(double value) {
		if (position == null)
			position = new double[3];

		position[1] = value;
	}

	public void setZ(double value) {
		if (position == null)
			position = new double[3];

		position[2] = value;
	}

	public Object[] getPosition() {
		Object p[] = new Object[3];
		p[0] = (Double) position[0];
		p[1] = (Double) position[1];
		p[2] = (Double) position[2];
		return p;
	}
}

class KeyValues extends HashMap<String, Object> {
	private static final long serialVersionUID = 5920553787913520204L;

	public String key;
	public int line;
	public int column;

	public void print() {
		System.err.printf("%s:%n", key);
		for (String k : keySet()) {
			System.err.printf("    %s: %s%n", k, get(k));
		}
	}

	public String optString(String key) throws IOException {
		Object o = get(key);

		if (o == null)
			return null;

		if (!(o instanceof String))
			throw new IOException(
					String.format(
							"%d:%d: expecting a string or number value for tag %s, got a list of values",
							line, column, key));

		remove(key);
		return (String) o;
	}

	protected String reqString(String key) throws IOException {
		Object o = get(key);

		if (o == null)
			throw new IOException(String.format(
					"%d:%d: expecting a tag %s but none found", line, column,
					key));

		if (!(o instanceof String))
			throw new IOException(
					String.format(
							"%d:%d: expecting a string or number value for tag %s, got a list of values",
							line, column, key));

		remove(key);

		return (String) o;
	}

	protected double reqNumber(String key) throws IOException {
		Object o = get(key);
		double v = 0.0;

		if (o == null)
			throw new IOException(String.format(
					"%d:%d: expecting a tag %s but none found", line, column,
					key));

		if (!(o instanceof String))
			throw new IOException(
					String.format(
							"%d:%d expecting a string or number value for tag %s, got a list of values",
							line, column, key));

		try {
			remove(key);
			v = Double.parseDouble((String) o);
		} catch (NumberFormatException e) {
			throw new IOException(String.format(
					"%d:%d: expecting a number value for tag %s, got a string",
					line, column, key));
		}

		return v;
	}

	protected KeyValues optKeyValues(String key) throws IOException {
		Object o = get(key);

		if (o == null)
			return null;

		if (!(o instanceof KeyValues))
			throw new IOException(
					String.format(
							"%d:%d: expecting a list of values for tag %s, got a string or number",
							line, column, key));

		remove(key);

		return (KeyValues) o;
	}

	protected KeyValues reqKeyValues(String key) throws IOException {
		Object o = get(key);

		if (o == null)
			throw new IOException(String.format(
					"%d:%d: expecting a tag %s but none found", line, column,
					key));

		if (!(o instanceof KeyValues))
			throw new IOException(
					String.format(
							"%d:%d: expecting a list of values for tag %s, got a string or number",
							line, column, key));

		remove(key);

		return (KeyValues) o;
	}

	protected void error(String message) throws IOException {
		throw new IOException(String.format("%d:%d: %s", line, column, message));
	}
}
