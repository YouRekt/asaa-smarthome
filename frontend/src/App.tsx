import Layout from "@/components/management-panel/layout";
import ManagementPanel from "@/components/management-panel/management-panel";
import { useStomp } from "@/hooks/use-stomp";
import {
	useStore,
	type Environment,
	type Message,
	type SystemStatus,
} from "@/hooks/use-store";
import { useEffect } from "react";

const SYSTEM = "/topic/system" as const;
const ENVIRONMENT = "/topic/environment" as const;
const AGENTS = "/topic/agent" as const;
const MESSAGES = "/topic/agent-message" as const;
const ERROR = "/topic/agent-error" as const;
const STATUS = "/topic/agent-status" as const;

const App = () => {
	const { connect, disconnect, subscribe } = useStomp();
	const { setEnvironment, addMessage, setSystemStatus } = useStore();

	useEffect(() => {
		connect();

		subscribe(SYSTEM, (message) => {
			console.log("System status update:", message);
			setSystemStatus(message.status as SystemStatus);
		});

		subscribe(ENVIRONMENT, (message) => {
			console.log("Environment update:", message);
			setEnvironment(message as Environment);
		});

		subscribe(AGENTS, (message) => {
			console.log("Agents update:", message);
			// Here dont update the store, just log the agents
		});

		subscribe(MESSAGES, (message) => {
			console.log("Messages update:", message);
			addMessage(message as Message);
		});

		subscribe(ERROR, (message) => {
			console.error("Error update:", message);
			// Here dont update the store, just log the error
		});

		subscribe(STATUS, (message) => {
			console.log("Status update:", message);
			// Here dont update the store, just log the status
		});

		return () => disconnect();
	}, [
		addMessage,
		connect,
		disconnect,
		setEnvironment,
		setSystemStatus,
		subscribe,
	]);

	return (
		<Layout>
			<ManagementPanel />
		</Layout>
	);
};
export default App;
