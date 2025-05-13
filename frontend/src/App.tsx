import { useEffect } from "react";
import EnvironmentViewer from "@/components/environment-viewer";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import { FloorPlan } from "@/components/floor-plan";
import useStore from "@/hooks/use-store";
import useStomp from "@/hooks/use-stomp";
import { Client } from "@stomp/stompjs";
import { Errors } from "@/components/errors";
import { Messages } from "@/components/messages";
import { Agents } from "@/components/agents";

export default function App() {
	const {
		setEnvironment,
		setSelectedRoom,
		setIsRunning,
		isRunning,
		selectedRoom,
		addAgent,
		setError,
		selectedAgent,
		setAgentMessage,
		deselectAgent,
		clearAll,
		showErrors,
		setShowErrors,
		errors,
		setAgentStatus,
		environment,
	} = useStore();

	const { setClient, sendMessage } = useStomp();

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
				stompClient.subscribe("/topic/agent-status", (message) => {
					const data = JSON.parse(message.body);
					console.log(data);
					setAgentStatus(data);
				});
			},
		});

		setClient(stompClient);
		stompClient.activate();

		return () => {
			stompClient.deactivate();
		};
	}, [
		addAgent,
		setAgentMessage,
		setAgentStatus,
		setClient,
		setEnvironment,
		setError,
	]);

	const handleRoomClick = (id: string) => {
		setSelectedRoom(id);
		sendMessage("/app/human-location", { area: id });
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
		clearAll();

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
				<Button
					disabled={Object.keys(errors).length === 0}
					onClick={() => setShowErrors(!showErrors)}
				>
					{showErrors ? "Hide Errors" : "Show Errors"}
				</Button>
			</nav>
			<div className="h-[calc(100dvh-4rem)] flex relative">
				<FloorPlan
					onClick={handleRoomClick}
					humanLocation={environment?.humanLocation || "kitchen"}
				/>
				<div className="border-l p-4 flex flex-col gap-4">
					<h2 className="text-xl font-semibold mb-2">
						{selectedRoom ? `${selectedRoom}` : "Select a room"}
					</h2>
					<EnvironmentViewer />
					<Agents />
				</div>
				{selectedAgent && (
					<div className="flex flex-col gap-4">
						<Button onClick={() => deselectAgent()}>Hide</Button>
						<Messages />
					</div>
				)}
				{showErrors && <Errors />}
			</div>
		</div>
	);
}
