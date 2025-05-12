import { useEffect } from "react";
import EnvironmentViewer from "@/components/environment-viewer";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { FloorPlan } from "@/components/floor-plan";
import useStore from "@/hooks/use-store";

import Agents from "@/components/agents";
import { Client } from "@stomp/stompjs";
import { Errors } from "@/components/errors";
export default function App() {
	const {
		setEnvironment,
		setAgents,
		setSelectedRoom,
		setIsRunning,
		isRunning,
		selectedRoom,
		setAgentMessage,
		addAgent,
		setError,
	} = useStore();

	useEffect(() => {
		const stompClient = new Client({
			brokerURL: "ws://localhost:8080/ws",
			reconnectDelay: 5000,
			onConnect: () => {
				stompClient.subscribe("/topic/environment", (message) => {
					const data = JSON.parse(message.body);
					setEnvironment(data);
				});
				stompClient.subscribe("/topic/agent", (message) => {
					const data = JSON.parse(message.body);
					addAgent(data);
				});
				stompClient.subscribe("/topic/agent-message", (message) => {
					const data = JSON.parse(message.body);
					setAgentMessage(data.aid, data.timestamp, data.message);
				});
				stompClient.subscribe("/topic/agent-error", (message) => {
					const data = JSON.parse(message.body);
					setError(data.aid, data.timestamp, data.message);
				});
			},
		});

		stompClient.activate();

		return () => {
			stompClient.deactivate();
		};
	}, [addAgent, setAgentMessage, setEnvironment, setError]);

	const handleRoomClick = (id: string) => {
		setSelectedRoom(id);
	};

	const handleStart = async () => {
		const response = await fetch("/system/start", {
			method: "POST",
		});
		if (!response.ok) {
			toast.error("Failed to start the system");
			return;
		}
		setIsRunning(true);
		toast.success("System started");
	};

	const handleStop = async () => {
		const response = await fetch("/system/stop", {
			method: "POST",
		});
		if (!response.ok) {
			toast.error("Failed to stop the system");
			return;
		}
		setIsRunning(false);
		setAgents([]);
		toast.success("System stopped");
	};

	return (
		<div className="h-dvh">
			<nav className="flex items-center justify-center gap-4 p-4 border-b h-16">
				<h1 className="text-2xl font-bold mr-16">Smart Home</h1>
				<Button
					className="bg-green-500 hover:bg-green-600"
					disabled={isRunning}
					onClick={handleStart}
				>
					Start
				</Button>
				<Button
					className="bg-red-500 hover:bg-red-600"
					disabled={!isRunning}
					onClick={handleStop}
				>
					Stop
				</Button>
			</nav>
			<div className="h-[calc(100dvh-4rem)] flex relative">
				<FloorPlan onClick={handleRoomClick} />
				<div className="border-l p-4 flex flex-col gap-4">
					<h2 className="text-xl font-semibold mb-2">
						{selectedRoom ? `${selectedRoom}` : "Select a room"}
					</h2>
					<EnvironmentViewer />
					<Agents />
				</div>
				<div>
					<Errors />
				</div>
			</div>
		</div>
	);
}
