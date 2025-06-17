import { Button } from "@/components/ui/button";
import { useStore } from "@/hooks/use-store";
import { Square } from "lucide-react";
import { useCallback } from "react";

const SystemStatus = () => {
	const { systemStatus, setSystemStatus } = useStore();

	const getSystemStatusColor = useCallback(() => {
		switch (systemStatus) {
			case "running":
				return "bg-green-500";
			case "starting":
			case "stopping":
				return "bg-yellow-500";
			case "stopped":
				return "bg-red-500";
		}
	}, [systemStatus]);

	const getSystemStatusText = useCallback(() => {
		switch (systemStatus) {
			case "running":
				return "Running";
			case "starting":
				return "Starting...";
			case "stopping":
				return "Stopping...";
			case "stopped":
				return "Stopped";
		}
	}, [systemStatus]);

	return (
		<div className="mx-4 mt-4 p-3 bg-accent rounded-lg">
			<div className="flex items-center gap-2">
				<div
					className={`w-2 h-2 rounded-full ${getSystemStatusColor()}`}
				/>
				<span className="text-sm font-medium">
					{getSystemStatusText()}
				</span>
			</div>
			{systemStatus === "running" && (
				<Button
					variant="ghost"
					size="sm"
					className="w-full mt-2 hover:text-destructive"
					onClick={() => {
						setSystemStatus("stopping");
						setTimeout(() => {
							setSystemStatus("stopped");
						}, 1000);
					}}
				>
					<Square className="size-3 mr-1" />
					Stop System
				</Button>
			)}
		</div>
	);
};
export default SystemStatus;
