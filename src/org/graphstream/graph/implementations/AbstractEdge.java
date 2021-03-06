package org.graphstream.graph.implementations;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SourceBase.ElementType;

/**
 * <p>
 * This class provides a basic implementation of {@code Edge} interface, to
 * minimize the effort required to implement this interface.
 * </p>
 * 
 * <p>
 * Although this class is abstract it implements all the methods of
 * {@link org.graphstream.graph.Edge} and
 * {@link org.graphstream.graph.implementations.AbstractElement}. It has a low
 * memory overhead (3 references and a boolean as fields). All {@code Edge}
 * methods are executed in O(1) time.
 * </p>
 */
public class AbstractEdge extends AbstractElement implements Edge {

	// *** Fields ***

	/**
	 * The source node
	 */
	protected AbstractNode source;

	/**
	 * The target node
	 */
	protected AbstractNode target;

	/**
	 * Is this edge directed ?
	 */
	protected boolean directed;

	/**
	 * The graph to which this edge belongs
	 */
	protected AbstractGraph graph;

	// *** Constructors ***

	/**
	 * Constructs a new edge. This constructor copies the parameters into the
	 * corresponding fields.
	 * 
	 * @param id
	 *            Unique identifier of this edge.
	 * @param source
	 *            Source node.
	 * @param target
	 *            Target node.
	 * @param directed
	 *            Indicates if the edge is directed.
	 */
	protected AbstractEdge(String id, AbstractNode source, AbstractNode target,
			boolean directed) {
		super(id);
		assert source != null && target != null : "An edge cannot have null endpoints";
		this.source = source;
		this.target = target;
		this.directed = directed;
		this.graph = (AbstractGraph) source.getGraph();
	}


	// *** Inherited from AbstractElement ***

	@Override
	protected void attributeChanged(String sourceId, long timeId,
			String attribute, AttributeChangeEvent event, Object oldValue,
			Object newValue) {
		graph.listeners.sendAttributeChangedEvent(sourceId, timeId, getId(),
				ElementType.EDGE, attribute, event, oldValue,
				newValue);
	}

	/**
	 * @return The id of the parent graph
	 * @see org.graphstream.graph.implementations.AbstractElement#myGraphId()
	 */
	@Override
	protected String myGraphId() {
		return graph.getId();
	}

	/**
	 * This implementation calls the corresponding method of the parent graph
	 * 
	 * @see org.graphstream.graph.implementations.AbstractElement#newEvent()
	 */
	@Override
	protected long newEvent() {
		return graph.newEvent();
	}

	/**
	 * This implementation calls the corresponding method of the parent graph
	 * 
	 * @see org.graphstream.graph.implementations.AbstractElement#nullAttributesAreErrors()
	 */
	@Override
	protected boolean nullAttributesAreErrors() {
		return graph.nullAttributesAreErrors();
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s-%s%s]", getId(), source, directed ? ">"
				: "-", target);
	}

	// *** Inherited from Edge ***

	@SuppressWarnings("unchecked")
	public <T extends Node> T getNode0() {
		return (T) source;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getNode1() {
		return (T) target;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getOpposite(Node node) {
		if (node == source)
			return (T) target;
		if (node == target)
			return (T) source;
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getSourceNode() {
		return (T) source;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getTargetNode() {
		return (T) target;
	}

	public boolean isDirected() {
		return directed;
	}

	public boolean isLoop() {
		return source == target;
	}
}
