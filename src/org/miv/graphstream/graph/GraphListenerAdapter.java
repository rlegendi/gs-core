/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.miv.graphstream.graph;

/**
 * A void implementation of {@link org.miv.graphstream.graph.GraphListener} that a
 * 
 * Inherit the class and override some of its methods so as to handle some of the services of the
 * <code>GraphListener</code> interface.
 * 
 * @since 2008/08/01
 * @see org.miv.graphstream.graph.GraphListener
 * 
 */
public class GraphListenerAdapter implements GraphListener
{

	public void edgeAdded( String graphId, String edgeId, String fromNodeId, String toNodeId,
            boolean directed )
    {
    }

	public void edgeRemoved( String graphId, String edgeId )
    {
    }

	public void edgeAttributeAdded( String graphId, String edgeId, String attribute, Object value )
    {
    }

	public void edgeAttributeChanged( String graphId, String edgeId, String attribute, Object oldValue, Object newValue )
    {
    }

	public void edgeAttributeRemoved( String graphId, String edgeId, String attribute )
    {
    }

	public void nodeAdded( String graphId, String nodeId )
    {
    }

	public void nodeRemoved( String graphId, String nodeId )
    {
    }

	public void nodeAttributeAdded( String graphId, String nodeId, String attribute, Object value )
    {
    }

	public void nodeAttributeChanged( String graphId, String nodeId, String attribute, Object oldValue, Object newValue )
    {
    }

	public void nodeAttributeRemoved( String graphId, String nodeId, String attribute )
    {
    }

	public void graphAttributeAdded( String graphId, String attribute, Object value )
    {
    }

	public void graphAttributeChanged( String graphId, String attribute, Object oldValue, Object newValue )
    {
    }

	public void graphAttributeRemoved( String graphId, String attribute )
    {
    }

	public void stepBegins( String graphId, double time )
    {
    }
}