package sns.socket.cmd;

public class Command {
	// Backend command
    public static final String CMD_BROADCAST = "BROADCAST";
	//CLIENT
	public static final String CLIENT_VERIFY_TOKEN = "CLIENT_VERIFY_TOKEN";
	public static final String CLIENT_PING = "CLIENT_PING";

	// PROCESS REQUEST

	public static final String CLIENT_RECEIVED_PUSH_NOTIFICATION = "CLIENT_RECEIVED_PUSH_NOTIFICATION";

	public static final String TEST_CMD = "TEST_CMD";
	public static final String CMD_COMPLETED_PROCESS_REQUEST = "CMD_COMPLETED_PROCESS_REQUEST";

	public static boolean ignoreToken(String cmd){
		switch (cmd){
			case TEST_CMD:
				return true;
			default:
				return false;
		}
	}
}
