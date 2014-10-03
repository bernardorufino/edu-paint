package br.com.bernardorufino.paint.figures;

import br.com.bernardorufino.paint.ext.Edge;
import br.com.bernardorufino.paint.ext.Pack;
import br.com.bernardorufino.paint.ext.Persistable;
import br.com.bernardorufino.paint.ext.Point;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public class Polygon implements Persistable {

    private List<Point> mVertices;

    public Polygon(Iterable<? extends Point> vertices) {
        mVertices = ImmutableList.copyOf(vertices);
        checkArgument(mVertices.size() >= 2, "A generalized polygon must have at least two vertices");
    }

    protected Polygon(Pack in) {
         mVertices = ImmutableList.copyOf(in.readPersistables(Point.CREATOR));
    }

    @Override
    public void persist(Pack out) {
        out.writePersistables(mVertices);
    }

    public int getYMax() {
        return mVertices.stream().map(p -> p.y).max(Ordering.natural()).get();
    }

    public int getYMin() {
        return mVertices.stream().map(p -> p.y).min(Ordering.natural()).get();
    }

    public List<Point> getVertices() {
        return ImmutableList.copyOf(mVertices);
    }

    private List<Edge> getEdges() {
        List<Edge> edges = new ArrayList<>(mVertices.size());
        for (int i = 0; i < mVertices.size(); i++) {
            Edge edge;
            if (mVertices.get(i).y < mVertices.get((i + 1) % (mVertices.size())).y) {
                edge = new Edge(mVertices.get(i), mVertices.get((i + 1) % (mVertices.size())));
            } else {
                edge = new Edge(mVertices.get((i + 1) % (mVertices.size())), mVertices.get(i));
            }
            edges.add(edge);
        }
        return edges;
    }

    public List<Edge> getSortedEdges() {
        List<Edge> edges = getEdges();
        edges.sort((a, b) -> Integer.compare(a.p1.y, b.p1.y));
        return edges;
    }

    public static final Creator<Polygon> CREATOR = new Creator<Polygon>() {
        @Override
        public Polygon create(Pack in) {
            return new Polygon(in);
        }
    };
}
