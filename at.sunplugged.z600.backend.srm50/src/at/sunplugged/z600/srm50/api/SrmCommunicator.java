package at.sunplugged.z600.srm50.api;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

/**
 * This Interface represents a communication point for the SRM-50-3LD SN 105598.
 * 
 * @author Jascha Riedel
 *
 */
public interface SrmCommunicator {

    /**
     * Returns currently saved data.
     *
     * @return {@linkplain List\<{@linkplain Double}\>} of values.
     * @throws IOException
     *             if retrieving failed.
     */
    public List<Double> getData() throws IOException;

    /**
     * Issues a command async.
     * 
     * @param string
     *            {@linkplain String} the command (A list of commands is
     *            available at
     *            {@linkplain at.sunplugged.z600.srm50.api.Commands}
     * @return {@linkplain Future<String>} future containing the answer when
     *         command is exectued.
     */
    public Future<String> issueCommandAsyn(String string);

}
