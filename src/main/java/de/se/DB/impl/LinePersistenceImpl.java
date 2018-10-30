package de.se.DB.impl;

import de.se.DB.GeneralPersistence;
import de.se.DB.LinePersistence;
import de.se.DB.hibernate_models.Linedb;
import de.se.DB.hibernate_models.Stopsinlinedb;
import de.se.data.Line;
import de.se.data.Route;
import de.se.data.Stop;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinePersistenceImpl extends GeneralPersistence implements LinePersistence {
    @Override
    public List<Line> fetchLines() {
        final Session session = getNewSession();
        List<Linedb> linesDBs = session.createQuery("FROM Linedb").list();

        List<Line> lines = new ArrayList<>();
        for (Linedb lineDB : linesDBs) {
            Line line = new Line(lineDB);
            List<Stop> stops = fetchStopsInLine(line.getId()); //we determine the stops for this line here
            Route route = new Route(stops);
            line.setRoute(route);
            lines.add(line);
        }

        session.close();
        return lines;
    }

    public void saveLine(Line line) {
        saveObjectToDatabase(line.getLineDB());
    }

    private List<Stop> fetchStopsInLine(String lineID) {
        final Session session = getNewSession();
        List<Stopsinlinedb> stopDBs = session.createQuery("FROM Stopsinlinedb where line = '" + lineID + "'").list();
        if (stopDBs.isEmpty()) {
            return new ArrayList<>();
        }

        //sort by position
        Collections.sort(stopDBs, (a, b) -> a.getPositionstoponline() < b.getPositionstoponline() ? -1 :
                a.getPositionstoponline() == b.getPositionstoponline() ? 0 : 1);

        List<Stop> stops = new ArrayList<>();
        for (Stopsinlinedb stopDB : stopDBs) {
            Stop stop = new Stop(stopDB.getStop());
            stops.add(stop);
        }

        session.close();
        return stops;
    }
}
