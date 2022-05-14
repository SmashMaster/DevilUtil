/*
 * Copyright (c) 2022 Sam Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.samrj.devil.math.topo;

import java.util.*;
import java.util.stream.Stream;

/**
 * Hash-map based directed acyclic graph. To be used mainly for topological sorting.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @param <TYPE> The kind of data to store in the node.
 */
public class DAG<TYPE> implements Iterable<TYPE>
{
    private static <T> Set<T> newSet()
    {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private final Map<TYPE, Vertex> vertices = new IdentityHashMap<>();
    private final List<Vertex> order = new ArrayList<>();
    private int modCount = 0;

    /**
     * Hash-map based directed graph vertex class.
     */
    private class Vertex implements Comparable<Vertex>
    {
        private final TYPE data;

        //Set of vertices that have an outgoing edge whose end is this.
        private final Set<Vertex> in = newSet();

        //Set of vertices that have an incoming edge whose start is this.
        private final Set<Vertex> out = newSet();

        //Index of this vertex in the order list.
        private int index;

        private Vertex(TYPE data)
        {
            this.data = data;
        }

        @Override
        public int hashCode()
        {
            return data.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;

            return data.equals(((Vertex)obj).data);
        }

        @Override
        public String toString()
        {
            return data.toString();
        }

        @Override
        public int compareTo(Vertex v)
        {
            return index < v.index ? -1 : 1;
        }
    }

    /**
     * Publicly exported edge.
     */
    public static class Edge<TYPE>
    {
        public final TYPE start, end;

        private Edge(TYPE s, TYPE e)
        {
            start = s; end = e;
        }

        @Override
        public String toString()
        {
            return "[" + start.toString() + " -> " + end.toString() + "]";
        }
    }

    /**
     * Adds vertex to DAG as an orphan. Does nothing if DAG already contains
     * vertex.
     *
     * @return true if the vertex was added
     */
    public boolean add(TYPE vertex)
    {
        if (vertex == null) throw new NullPointerException();
        if (vertices.containsKey(vertex)) return false;

        Vertex v = new Vertex(vertex);
        v.index = order.size();

        vertices.put(vertex, v);
        order.add(v);
        modCount++;

        return false;
    }

    /**
     * Removes the given vertex and any edges connected to it from this graph.
     *
     * @return true if the vertex was removed
     */
    public boolean remove(TYPE vertex)
    {
        if (vertex == null) throw new NullPointerException();

        Vertex v = vertices.remove(vertex);

        if (v == null) return false;

        order.remove(v.index);
        modCount++;

        int size = order.size();
        for (int i=v.index; i<size; i++) order.get(i).index = i;

        for (Vertex cn : v.out) cn.in.remove(v);
        for (Vertex pn : v.in) pn.out.remove(v);

        return true;
    }

    private void dfsForward(Vertex current, int rightBound, Set<Vertex> result)
    {
        result.add(current);
        for (Vertex neighbor : current.out)
            if (neighbor.index <= rightBound && !result.contains(neighbor))
                dfsForward(neighbor, rightBound, result);
    }

    private void dfsBackward(Vertex current, int leftBound, Set<Vertex> result)
    {
        result.add(current);
        for (Vertex neighbor : current.in)
            if (neighbor.index >= leftBound && !result.contains(neighbor))
                dfsBackward(neighbor, leftBound, result);
    }

    private boolean edge(Vertex start, Vertex end, boolean safe)
    {
        if (start == null || end == null) throw new IllegalArgumentException();
        if (start.out.contains(end)) return false;

        //Check if order is not invalidated.
        if (start.index < end.index)
        {
            start.out.add(end);
            end.in.add(start);
            modCount++;
            return true;
        }

        //Populate left group. Considered using TreeMap for this--but only need
        //to sort once. IdentityHashMaps are deadly fast anyway.
        Set<Vertex> leftGroup = newSet();
        dfsForward(end, start.index, leftGroup);

        //Ensure no cycle would be created by adding this edge.
        if (leftGroup.contains(start))
        {
            if (safe) throw new CyclicGraphException();
            else return false;
        }

        //Populate right group.
        Set<Vertex> rightGroup = newSet();
        dfsBackward(start, end.index, rightGroup);

        //Now the fun begins: We need to reassign indices to each vertex in
        //leftGroup and rightGroup, such that:
        // * order is maintained within each group
        // * rightGroup is now entirely to the left of leftGroup

        //First, sort our groups by index.
        List<Vertex> leftSorted = new ArrayList<>(leftGroup);
        Collections.sort(leftSorted);
        List<Vertex> rightSorted = new ArrayList<>(rightGroup);
        Collections.sort(rightSorted);

        //Then, pool together all indices.
        int[] indices = new int[leftSorted.size() + rightSorted.size()];

        int i = 0;
        for (Vertex v : leftSorted) indices[i++] = v.index;
        for (Vertex v : rightSorted) indices[i++] = v.index;
        Arrays.sort(indices);

        //Now swap the right with the left.
        i = 0;
        for (Vertex v : rightSorted) order.set(v.index = indices[i++], v);
        for (Vertex v : leftSorted) order.set(v.index = indices[i++], v);

        //Finally, add the edge and we're done!
        start.out.add(end);
        end.in.add(start);
        modCount++;
        return true;
    }

    /**
     * Adds an edge to the DAG.
     *
     * @return true if the edge was added
     * @throws CyclicGraphException if adding this edge would make the
     *         graph cyclic
     */
    public boolean addEdgeSafe(TYPE start, TYPE end)
    {
        if (start == null || end == null) throw new NullPointerException();

        return edge(vertices.get(start), vertices.get(end), true);
    }

    /**
     * Adds an edge to the DAG if the edge doesn't already exist, and
     * adding that edge would not make the graph cyclic.
     *
     * @return true if the edge was added
     */
    public boolean addEdge(TYPE start, TYPE end)
    {
        if (start == null || end == null) throw new NullPointerException();

        return edge(vertices.get(start), vertices.get(end), false);
    }

    /**
     * Returns true and removes the given edge from this DAG if it exists.
     * Otherwise, returns false.
     *
     * @return true if the edge was removed.
     */
    public boolean removeEdge(TYPE start, TYPE end)
    {
        if (start == null || end == null) throw new NullPointerException();

        Vertex sv = vertices.get(start);
        Vertex ev = vertices.get(end);

        if (sv == null || ev == null) return false;
        if (!sv.out.contains(ev)) return false;

        sv.out.remove(ev);
        ev.in.remove(sv);

        modCount++;
        return true;
    }

    /**
     * Performs a topological sort on this DAG and returns the result in a list,
     * in ascending order of depth.
     */
    public List<TYPE> sort()
    {
        int size = order.size();
        Object[] array = new Object[size];
        for (int i=0; i<size; i++) array[i] = order.get(i).data;
        return (List<TYPE>)Arrays.asList(array);
    }

    /**
     * Returns a set containing all sinks: vertices that have no outgoing edges.
     */
    public Set<TYPE> getSinks()
    {
        Set<TYPE> out = newSet();
        for (Vertex v : vertices.values()) if (v.out.isEmpty()) out.add(v.data);
        return out;
    }

    /**
     * Returns a set containing all sources: vertices that have no incoming
     * edges.
     */
    public Set<TYPE> getSources()
    {
        Set<TYPE> out = newSet();
        for (Vertex v : vertices.values()) if (v.in.isEmpty()) out.add(v.data);
        return out;
    }

    /**
     * Returns a list containing every edge represented as an array. The first
     * element in each array is the start of the edge. The second is the end of
     * the edge.
     */
    public List<Edge<TYPE>> getEdges()
    {
        List<Edge<TYPE>> out = new ArrayList<>();
        for (Vertex st : vertices.values()) for (Vertex en : st.out)
            out.add(new Edge<>(st.data, en.data));
        return out;
    }

    /**
     * Same as getEdges, but the edges are topologically sorted.
     */
    public List<Edge<TYPE>> getSortedEdges()
    {
        List<Edge<TYPE>> out = new ArrayList<>();
        for (Vertex st : order)
            for (Vertex en : st.out)
                out.add(new Edge<>(st.data, en.data));
        return out;
    }

    public boolean contains(TYPE vertex)
    {
        return vertices.containsKey(vertex);
    }

    public boolean hasEdge(TYPE start, TYPE end)
    {
        Vertex pv = vertices.get(start);
        if (pv == null) return false;
        Vertex cv = vertices.get(end);
        if (cv == null) return false;

        return pv.out.contains(cv);
    }

    public Set<TYPE> getIn(TYPE vertex)
    {
        Vertex v = vertices.get(vertex);
        if (v == null) return null;

        Set<TYPE> out = newSet();
        for (Vertex pv : v.in) out.add(pv.data);
        return out;
    }

    public Set<TYPE> getOut(TYPE vertex)
    {
        Vertex v = vertices.get(vertex);
        if (v == null) return null;

        Set<TYPE> out = newSet();
        for (Vertex cv : v.out) out.add(cv.data);
        return out;
    }

    public Set<TYPE> getAll()
    {
        Set<TYPE> out = newSet();
        for (TYPE data : vertices.keySet()) out.add(data);
        return out;
    }

    public Stream<TYPE> stream()
    {
        return vertices.keySet().stream();
    }

    public Stream<Edge<TYPE>> edgeStream()
    {
        return vertices.entrySet().stream()
                .map(e -> e.getValue())
                .flatMap(start -> start.out.stream()
                        .map(end -> new Edge<>(start.data, end.data)));
    }

    /**
     * Returns a new DAG with the given node as the only sink; all others are
     * ignored.
     */
    public DAG<TYPE> subgraph(TYPE sink)
    {
        DAG<TYPE> graph = new DAG<>();
        trimVisit(vertices.get(sink), graph);

        for (TYPE vert : graph.getAll())
            for (TYPE in : getIn(vert))
                if (graph.contains(in)) graph.addEdge(in, vert);

        return graph;
    }

    private void trimVisit(Vertex v, DAG<TYPE> graph)
    {
        graph.add(v.data);
        for (Vertex in : v.in) trimVisit(in, graph);
    }

    public void clear()
    {
        vertices.clear();
        order.clear();
    }

    public int size()
    {
        return order.size();
    }

    public boolean isEmpty()
    {
        return order.isEmpty();
    }

    private class DAGIterator implements Iterator<TYPE>
    {
        private int expectedModCount = modCount;
        private int cursor = 0;

        private void checkModCount()
        {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
        }

        @Override
        public boolean hasNext()
        {
            return cursor != size();
        }

        @Override
        public TYPE next()
        {
            return order.get(cursor++).data;
        }
    }

    @Override
    public Iterator<TYPE> iterator()
    {
        return new DAGIterator();
    }
}
