package de.se.DB;

import de.se.DB.impl.LinePersistenceImpl;
import org.junit.Ignore;
import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LinePersistenceImplTest {
    private LinePersistence linePersistence = new LinePersistenceImpl();

    @Test
    public void testFetchLines() throws Exception {
        assertNotNull(linePersistence.fetchLines());
        assertTrue(linePersistence.fetchLines().size() > 0);
    }

}
