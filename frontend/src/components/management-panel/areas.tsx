import { Badge } from "@/components/ui/badge";
import { useStore } from "@/hooks/use-store";
import { Home } from "lucide-react";

const Areas = () => {
	const { areas, selectedArea, setSelectedArea, agents } = useStore();

	return (
		<div className="mt-8 flex-grow flex flex-col">
			<nav className="flex-1 px-2 flex flex-col gap-1">
				<div className="px-3 py-2 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
					Rooms
				</div>
				{areas.map((area) => (
					<button
						key={area.name}
						onClick={() => setSelectedArea(area.name)}
						className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md w-full text-left ${
							selectedArea === area.name
								? "bg-primary-bg text-primary"
								: "text-muted-foreground hover:bg-muted hover:text-foreground"
						}`}
					>
						<Home className="mr-3 size-5" />
						<span className="flex-1">{area.name}</span>
						<Badge variant="secondary" className="ml-2">
							{
								agents.filter(
									(agent) => agent.area === area.name
								).length
							}
						</Badge>
					</button>
				))}
			</nav>
		</div>
	);
};
export default Areas;
