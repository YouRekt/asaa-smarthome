import { Progress } from "@/components/ui/progress";
import { useStomp } from "@/hooks/use-stomp";
import { useStore } from "@/hooks/use-store";
import {
	AlertTriangle,
	CheckCircle,
	CircleDollarSign,
	Clock,
	Thermometer,
	Wifi,
	WifiOff,
	Zap,
} from "lucide-react";

const Header = () => {
	const { areas, selectedArea, environment } = useStore();
	const { isConnected } = useStomp();

	const currentRoom = areas.find((area) => area.name === selectedArea);

	// Calculate task completion percentage
	const totalTasks =
		(environment?.performedTasks || 0) + (environment?.errorTasks || 0);
	const successRate =
		totalTasks > 0
			? ((environment?.performedTasks || 0) / totalTasks) * 100
			: 0;
	const errorRate =
		totalTasks > 0
			? ((environment?.errorTasks || 0) / totalTasks) * 100
			: 0;

	// Calculate power usage percentage
	const powerUsagePercent = environment?.maxPowerCapacity
		? (environment.currentPowerConsumption / environment.maxPowerCapacity) *
		  100
		: 0;

	return (
		<header className="bg-card border-b">
			<div className="px-4 sm:px-6 lg:px-8">
				<div className="flex items-center justify-between h-16">
					<div className="flex items-center gap-4">
						<h1 className="text-2xl font-semibold text-card-foreground ml-12 md:ml-0">
							{currentRoom?.name}
						</h1>

						<div className="flex items-center gap-2">
							{isConnected ? (
								<Wifi className="size-4 text-green-500" />
							) : (
								<WifiOff className="size-4 text-red-500" />
							)}
							<span className="text-sm text-muted-foreground">
								{isConnected ? "Connected" : "Disconnected"}
							</span>
						</div>
					</div>

					{/* Simulation stats */}
					{isConnected && environment && (
						<div className="hidden lg:flex items-center gap-6">
							{/* Time */}
							<div className="flex items-center gap-2">
								<Clock className="size-4 text-muted-foreground" />
								<span className="text-sm font-medium">
									{environment.time}
								</span>
							</div>

							{/* Temperature */}
							<div className="flex items-center gap-2">
								<Thermometer className="size-4 text-muted-foreground" />
								<span className="text-sm font-medium">
									{(
										currentRoom?.attributes
											.temperature as number
									)?.toFixed(1) || "N/A"}
									°C
								</span>
							</div>

							{/* Credits */}
							<div className="flex items-center gap-2">
								<CircleDollarSign className="size-4 text-muted-foreground" />
								<span className="text-sm font-medium">
									{environment.credits}
								</span>
							</div>

							{/* Power Usage */}
							<div className="flex items-center gap-2">
								<div className="flex flex-col items-center">
									<div className="flex items-center gap-1">
										<Zap className="size-4 text-muted-foreground" />
										<span className="text-sm font-medium">
											{
												environment.currentPowerConsumption
											}{" "}
											/ {environment.maxPowerCapacity} W
										</span>
									</div>
									<Progress
										value={powerUsagePercent}
										className="w-20 h-1"
									/>
								</div>
							</div>

							{/* Task Statistics */}
							<div className="flex items-center gap-4">
								<div className="flex items-center gap-1">
									<CheckCircle className="size-4 text-green-500" />
									<span className="text-sm font-medium text-green-600">
										{environment.performedTasks}
									</span>
									<span className="text-xs text-muted-foreground">
										({successRate.toFixed(1)}%)
									</span>
								</div>
								<div className="flex items-center gap-1">
									<AlertTriangle className="size-4 text-red-500" />
									<span className="text-sm font-medium text-red-600">
										{environment.errorTasks}
									</span>
									<span className="text-xs text-muted-foreground">
										({errorRate.toFixed(1)}%)
									</span>
								</div>
							</div>
						</div>
					)}
				</div>
			</div>
		</header>
	);
};

export default Header;
