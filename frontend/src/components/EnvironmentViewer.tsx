import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

type Environment = {
	time: string;
	credits: number;
	timeDelta: number;
	maxPowerCapacity: number;
	currentPowerConsumption: number;
	areas: {
		name: string;
		attributes: Record<string, string | number | boolean>;
	}[];
};

type AgentMessage = {
	agentName: string;
	area: string;
	message: string;
};

const EnvironmentViewer = () => {
	const [env, setEnv] = useState<Environment | null>(null);
	const [agentMessages, setAgentMessages] = useState<AgentMessage[]>([]);

	useEffect(() => {
		const stompClient = new Client({
			brokerURL: "ws://localhost:8080/ws",
			reconnectDelay: 5000,
			onConnect: () => {
				stompClient.subscribe("/topic/environment", (message) => {
					const data = JSON.parse(message.body);
					//console.log(data);
					setEnv(data);
				});
				stompClient.subscribe("/topic/agent", (message) => {
					const data = JSON.parse(message.body);
					setAgentMessages((prev) => [...prev, data]);
				});

				// Optional: request initial snapshot
				//stompClient.publish({ destination: "/app/env", body: "" });
			},
			debug: (str) => console.log("[STOMP]", str),
		});

		stompClient.activate();

		return () => {
			stompClient.deactivate();
		};
	}, []);

	useEffect(() => {
		if (!env) return;

		console.log(env);
	}, [env]);

	if (!env) return <p>Waiting for environment data...</p>;

	return (
		<div>
			<h2>Environment Status</h2>
			<p>
				<strong>Time:</strong> {env.time}
			</p>
			<p>
				<strong>credits:</strong> {env.credits}
			</p>
			<p>
				<strong>Delta:</strong> {env.timeDelta}
			</p>

			<h3>Areas:</h3>
			<ul>
				{env.areas.map((area) => (
					<li key={area.name}>
						<strong>{area.name}</strong>
						<ul>
							{Object.entries(area.attributes).map(
								([key, value]) => (
									<li key={key}>
										{key}: {JSON.stringify(value)}
									</li>
								)
							)}
						</ul>
					</li>
				))}
			</ul>

			<h3>Agent Messages:</h3>
			<ul>
				{agentMessages.map((message) => (
					<li key={message.agentName}>
						{message.agentName}: {message.area} - {message.message}
					</li>
				))}
			</ul>
		</div>
	);
};

export default EnvironmentViewer;
