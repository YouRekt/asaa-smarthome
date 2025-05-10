import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";

type Agent = {
	name: string;
	area: string;
	status: string;
};

const Agents = () => {
	const [agents, setAgents] = useState<Agent[]>([]);

	useEffect(() => {
		const stompClient = new Client({
			brokerURL: "ws://localhost:8080/ws",
			reconnectDelay: 5000,
			onConnect: () => {
				stompClient.subscribe("/topic/agent", (message) => {
					const data = JSON.parse(message.body);
					setAgents((prev) => {
						// Update existing agent or add new one
						const existingIndex = prev.findIndex(
							(a) => a.name === data.agentName
						);
						if (existingIndex >= 0) {
							const newAgents = [...prev];
							newAgents[existingIndex] = {
								name: data.agentName,
								area: data.area,
								status: data.message,
							};
							return newAgents;
						}
						return [
							...prev,
							{
								name: data.agentName,
								area: data.area,
								status: data.message,
							},
						];
					});
				});
			},
			debug: (str) => console.log("[STOMP]", str),
		});

		stompClient.activate();

		return () => {
			stompClient.deactivate();
		};
	}, []);

	return (
		<div className="agents-container">
			<h2>Agents</h2>
			<div className="agents-grid">
				{agents.map((agent) => (
					<div key={agent.name} className="agent-card">
						<h3>{agent.name}</h3>
						<p>Area: {agent.area}</p>
						<p>Status: {agent.status}</p>
					</div>
				))}
			</div>
		</div>
	);
};

export default Agents;
