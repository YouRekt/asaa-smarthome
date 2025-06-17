import { useStore } from "@/hooks/use-store";
import {
	Clock,
	CreditCard,
	Thermometer,
	Wifi,
	WifiOff,
	Zap,
} from "lucide-react";

const Header = () => {
	const { areas, selectedArea, isConnected, environment } = useStore();

	const currentRoom = areas.find((area) => area.name === selectedArea);

	return (
		<header className="bg-card border-b">
			<div className="px-4 sm:px-6 lg:px-8">
				<div className="flex items-center justify-between h-16">
					<div className="flex items-center space-x-4">
						<h1 className="text-2xl font-semibold text-card-foreground ml-12 md:ml-0">
							{currentRoom?.name}
						</h1>
						<div className="flex items-center space-x-2">
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
					{isConnected && (
						<div className="hidden lg:flex items-center space-x-6">
							<div className="flex items-center space-x-2">
								<Clock className="size-4 text-gray-400" />
								<span className="text-sm font-medium">
									{environment?.time}
								</span>
							</div>
							<div className="flex items-center space-x-2">
								<Thermometer className="size-4 text-gray-400" />
								<span className="text-sm font-medium">
									{(
										currentRoom?.attributes
											.temperature as number
									).toFixed(1)}
									°C
								</span>
							</div>
							<div className="flex items-center space-x-2">
								<CreditCard className="size-4 text-gray-400" />
								<span className="text-sm font-medium">
									{environment?.credits}
								</span>
							</div>
							<div className="flex items-center space-x-2">
								<Zap className="size-4 text-gray-400" />
								<span className="text-sm font-medium">
									{environment?.currentPowerConsumption.toFixed(
										1
									)}
									kW
								</span>
							</div>
						</div>
					)}
				</div>
			</div>
		</header>
	);
};
export default Header;
