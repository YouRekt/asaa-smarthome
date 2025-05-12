import { Agent } from "@/components/agent";
import useStore from "@/hooks/use-store";
const Agents = () => {
	const { agents } = useStore();

	return (
		<>
			{agents.length > 0 &&
				agents.map((agent) => <Agent key={agent.aid} agent={agent} />)}
		</>
	);
};

export default Agents;
