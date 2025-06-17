import { Badge } from "@/components/ui/badge";
import { useStore } from "@/hooks/use-store";
import { Home, Thermometer, User } from "lucide-react";

const Areas = () => {
	const { areas, selectedArea, setSelectedArea, agents, environment } =
		useStore();

	return (
		<div className="mt-8 flex-grow flex flex-col">
			<nav className="flex-1 px-2 flex flex-col gap-1">
				<div className="px-3 py-2 text-xs font-semibold text-muted-foreground uppercase tracking-wider">
					Rooms
				</div>
				{areas.map((area) => {
					const isHumanLocation =
						environment?.humanLocation &&
						environment.humanLocation !== "null" &&
						environment.humanLocation.toLowerCase() ===
							area.name.toLowerCase();
					const agentCount = agents.filter(
						(agent) => agent.area === area.name
					).length;

					return (
						<button
							key={area.name}
							onClick={() => setSelectedArea(area.name)}
							className={`group flex items-center px-2 py-2 text-sm font-medium rounded-md w-full text-left ${
								selectedArea === area.name
									? "bg-primary-bg text-primary"
									: "text-muted-foreground hover:bg-muted hover:text-foreground"
							}`}
						>
							<div className="flex items-center mr-3">
								<Home className="size-5" />
								{isHumanLocation && (
									<User className="size-3 text-blue-500 ml-1 -mr-1" />
								)}
							</div>
							<div className="flex-1">
								<div className="flex items-center gap-2">
									<span>{area.name}</span>
								</div>
								<div className="flex items-center gap-1 text-xs text-muted-foreground">
									<Thermometer className="size-3" />
									<span>
										{area.attributes.temperature.toFixed(1)}
										°C
									</span>
								</div>
							</div>
							<Badge variant="secondary" className="ml-2">
								{agentCount}
							</Badge>
						</button>
					);
				})}
			</nav>
		</div>
	);
};

export default Areas;
