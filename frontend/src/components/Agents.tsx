import { useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import { Button } from "@/components/ui/button";

type Agent = {
	aid: string;
	name: string;
	area: string;
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
					setAgents((prevAgents) => [...prevAgents, data]);
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
		<>
			{agents.map((agent, index) => (
				<div
					key={agent.aid}
					className="absolute top-10 bg-red-500/25 rounded-md p-2"
					style={{
						left: `${index * 200 + 100}px`,
					}}
				>
					<h3 className="left-[100px]">{agent.name}</h3>
					<p>Area: {agent.area}</p>
					<Button>Do sth</Button>
				</div>
			))}
		</>
	);
};

export default Agents;
