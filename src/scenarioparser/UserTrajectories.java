package src.scenarioparser;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * User trajectory data class
 */
public class UserTrajectories
{
    private final int id;
    private final String type; // "Pedestrian" or "Motorist"
    private final Map<Double, double[]> trajectories; // <timestamp, <x, y>>

    public UserTrajectories(int p_id, String p_type, Map<Double, double[]> p_trajectories)
    {
        id = p_id;
        type = p_type;
        trajectories = p_trajectories;
    }

    public int id() { return id; }

    public String type() { return type; }

    public Map<Double, double[]> trajectory() { return trajectories; }

    @Override
    public String toString() {
        return "UserTrajectories [id=" + id + ", type=" + type + ", \n" +"trajectories <timestamp: [x, y]>=" + "\n\t" +
                trajectories.entrySet()
                        .stream()
                        .map(entry -> entry.getKey() + ": [" + entry.getValue()[0] + ", " + entry.getValue()[1] + "]")
                        .collect(Collectors.joining(",\n\t"))
                + "]";
    }

    /**
     * User trajectory data generator
     */
    public static final class Generator
    {
        private static int COUNTER = 0;

        Generator() {}

        public static int count() { return COUNTER; }

        public static void resetcount() { COUNTER = 0; }

        public static Map<Integer, UserTrajectories> generateFromCSV(String path)
        {
            // users map to store (id, user)
            Map<Integer, UserTrajectories> l_users = new ConcurrentHashMap<>();

            // load data and remove header
            List<String[]> l_data = TrajectoryParser.DEFAULT.loadtrajectories(path);
            l_data.remove(0);

            // group data by id
            Map<Integer, List<String[]>> l_usersData =
                    l_data.stream().collect(Collectors.groupingBy(s -> Integer.parseInt(s[1])));

            // Iterate over the user data create users and their respective trajectories
            // iteration goes in parallel to improve performance
            l_usersData.values()
                    .parallelStream()
                    .map(l_userData ->
                    {
                        COUNTER++;
                        return new UserTrajectories(
                                Integer.parseInt(l_userData.get(0)[1]),
                                l_userData.get(0)[2],
                                l_userData.stream()
                                        .collect(Collectors.toMap(
                                                row -> Double.parseDouble(row[0]),
                                                row -> new double[]{
                                                        Double.parseDouble(row[3]),
                                                        Double.parseDouble(row[4])}
                                        ))
                        );
                    })
                    .forEach(l_user -> l_users.put(l_user.id(), l_user));

            return l_users;
        }
    }
}