package org.cloudgraph.web.component;

import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.TickUnitSource;

public class VariableTickUnitSource implements TickUnitSource {

    /**
     * Returns the tick unit in the collection that is greater than or equal to (in size) the specified unit.
     */
    public TickUnit getCeilingTickUnit(TickUnit unit) {
        // TODO Auto-generated method stub
        //return new org.jfree.chart.axis.NumberTickUnit(100);
        //return null;
        if (unit.getSize() < 500)
            return new NumberTickUnit(100);
        else if (unit.getSize() < 1000)
            return new NumberTickUnit(1000);
        else 
            return new NumberTickUnit(5000);
    }

    /**
     * Returns the tick unit in the collection that is greater than or equal to the specified size.
     */
    public TickUnit getCeilingTickUnit(double size) {
        // TODO Auto-generated method stub
        //return new org.jfree.chart.axis.NumberTickUnit(100);
        //return null;
        if (size < 500)
            return new NumberTickUnit(100);
        else if (size < 1000)
            return new NumberTickUnit(1000);
        else 
            return new NumberTickUnit(5000);
    }

    /**
     * Returns a tick unit that is larger than the supplied unit.
     */
    public TickUnit getLargerTickUnit(TickUnit unit) {
        // TODO Auto-generated method stub
        //return new org.jfree.chart.axis.NumberTickUnit(100);
        if (unit.getSize() < 500)
            return new NumberTickUnit(100);
        else if (unit.getSize() < 1000)
            return new NumberTickUnit(1000);
        else 
            return new NumberTickUnit(5000);
    }

}
