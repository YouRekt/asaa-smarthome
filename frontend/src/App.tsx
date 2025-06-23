import Layout from "@/components/management-panel/layout";
import ManagementPanel from "@/components/management-panel/management-panel";
import { useStomp } from "@/hooks/use-stomp";
import {
	useStore,
	type AgentError,
	type AgentStatus,
	type Environment,
	type Message,
	type SystemStatus,
} from "@/hooks/use-store";
import { useEffect } from "react";

const SYSTEM = "/topic/system" as const;
const ENVIRONMENT = "/topic/environment" as const;
const MESSAGES = "/topic/agent-message" as const;
const ERROR = "/topic/agent-error" as const;
const STATUS = "/topic/agent-status" as const;

const App = () => {
	const { connect, disconnect, subscribe } = useStomp();
	const {
		setEnvironment,
		addMessage,
		setSystemStatus,
		updateAgentStatus,
		updateAreaAttributes,
		addError,
	} = useStore();

	useEffect(() => {
		connect();

		subscribe(SYSTEM, (message) => {
			setSystemStatus(message.status as SystemStatus);
		});

		subscribe(ENVIRONMENT, (message) => {
			// Extract environment data without areas
			const { areas, ...environmentData } = message;
			setEnvironment(environmentData as Environment);

			// Update area attributes separately
			if (areas && Array.isArray(areas)) {
				areas.forEach((area) => {
					if (area.name && area.attributes) {
						updateAreaAttributes(area.name, area.attributes);
					}
				});
			}
		});

		subscribe(MESSAGES, (message) => {
			addMessage(message as Message);
		});

		subscribe(ERROR, (message) => {
			// Add error to store
			addError(message as AgentError);
		});

		subscribe(STATUS, (message) => {
			const { aid, ...status } = message;
			updateAgentStatus(status as AgentStatus, aid as string);
		});

		return () => disconnect();
	}, [
		addMessage,
		connect,
		disconnect,
		setEnvironment,
		setSystemStatus,
		subscribe,
		updateAgentStatus,
		updateAreaAttributes,
		addError,
	]);

	return (
		<Layout>
			<ManagementPanel />
		</Layout>
	);
};
export default App;
