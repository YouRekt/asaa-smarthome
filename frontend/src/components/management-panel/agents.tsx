import AgentCard from "@/components/management-panel/agent-card";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { useStore } from "@/hooks/use-store";

const Agents = () => {
	const { areas, selectedArea, agents } = useStore();

	const currentRoom = areas.find((area) => area.name === selectedArea);

	const currentRoomAgents = agents.filter(
		(agent) => agent.area === selectedArea
	);

	return (
		<Card>
			<CardHeader>
				<CardTitle>Agents</CardTitle>
				<CardDescription>Agents in {currentRoom?.name}</CardDescription>
			</CardHeader>
			<CardContent>
				<div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-4 place-items-center">
					{currentRoomAgents.map((agent) => (
						<AgentCard key={agent.aid} agent={agent} />
					))}
				</div>
			</CardContent>
		</Card>
	);
};
export default Agents;
