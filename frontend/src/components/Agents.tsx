import { Agent } from "@/components/agent";
import useStore from "@/hooks/use-store";

export const Agents = () => {
	const { agents } = useStore();

	return (
		<div className="flex flex-col gap-2 overflow-y-auto">
			{agents.length > 0 &&
				agents.map((agent) => <Agent key={agent.aid} agent={agent} />)}
		</div>
	);
};
