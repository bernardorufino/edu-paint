package br.com.bernardorufino.paint.figures;


import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.grapher.Configuration;
import br.com.bernardorufino.paint.grapher.Grapher;

public interface Figure {

    public abstract void draw(Grapher grapher);

    public boolean isSelected(Point point);

    public void setConfiguration(Configuration configuration);

    public Configuration getConfiguration();

}
