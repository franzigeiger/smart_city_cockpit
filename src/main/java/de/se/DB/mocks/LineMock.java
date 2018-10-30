package de.se.DB.mocks;

import de.se.DB.LinePersistence;
import de.se.data.Line;
import de.se.data.Route;
import de.se.data.Stop;
import de.se.data.enums.VehicleEnum;

import java.util.ArrayList;
import java.util.List;

public class LineMock implements LinePersistence{
    @Override
    public List<Line> fetchLines() {
        List<Line> lines = new ArrayList<Line>();
       //more advanced Line for testing

        Line l1 = new Line("l1", "Line 1" , VehicleEnum.Bus.toString(),new Route(new ArrayList<Stop>()) , "red" );
        List<Stop> stops = new ArrayList<>();
        stops.add(new Stop("one", "name" , "123" , "456" , null));
        stops.add(new Stop("two","name" , "123" , "456" , null));
        stops.add(new Stop("three","name" , "123" , "456" , null));
        stops.add(new Stop("four","name" , "123" , "456" , null));
        Route r1 = new Route(stops);
        l1.setRoute(r1);
        lines.add(l1);

        lines.add( new Line("l2", "Line 2" , VehicleEnum.Bus.toString(),  new Route(new ArrayList<Stop>()) ,"blue" ));
        lines.add( new Line("l3", "Line 3" , VehicleEnum.Tube.toString(),  new Route(new ArrayList<Stop>()), "black" ));
        lines.add( new Line("l4", "Line 4" , VehicleEnum.Tube.toString(),  new Route(new ArrayList<Stop>()), "blassrosa" ));
        return lines;
    }

    @Override
    public void saveLine(Line line) {

    }
}
