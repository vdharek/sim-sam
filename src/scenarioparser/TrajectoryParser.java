package src.scenarioparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * parse trajectories data into a map that contains (id, user)
 */
public final class TrajectoryParser
{
    /**
     * singleton instance
     */
    public static final TrajectoryParser DEFAULT = new TrajectoryParser();


    TrajectoryParser() {}

    /**
     * load the trajectories data from a file and return a list of trajectories
     *
     * @param path file path
     * @return list of trajectories
     */
    public List<String[]> loadtrajectories(String path)
    {
        List<String[]> data = new ArrayList<>();
        String line;

        try (
                InputStream l_stream = getClass().getResourceAsStream(path);
                BufferedReader l_reader = new BufferedReader(new InputStreamReader(l_stream))
        )
        {
            while ((line = l_reader.readLine()) != null)
                data.add(line.split(","));
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        return data;
    }

}
