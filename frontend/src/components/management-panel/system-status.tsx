import { Button } from "@/components/ui/button";
import { useStomp } from "@/hooks/use-stomp";
import { useStore } from "@/hooks/use-store";
import { Square } from "lucide-react";
import { useCallback } from "react";
import { toast } from "sonner";

const SystemStatus = () => {
	const { systemStatus, setSystemStatus, resetAllState } = useStore();
	const { isConnected } = useStomp();

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

	async function handleStopSystem() {
		try {
			// Set stopping status first
			setSystemStatus("stopping");

			if (isConnected) {
				const response = await fetch("system/stop", {
					method: "POST",
				});
				if (!response.ok) {
					toast.error("Failed to stop the system");
					// Revert status if the API call failed
					setSystemStatus("running");
					return;
				}
			}

			// Reset all state when system stops
			resetAllState();
			toast.success("System stopped successfully");
		} catch (error) {
			console.error("Error stopping system:", error);
			toast.error("Failed to stop the system");
			// Revert status if there was an error
			setSystemStatus("running");
		}
	}

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
					onClick={handleStopSystem}
				>
					<Square className="size-3 mr-1" />
					Stop System
				</Button>
			)}
		</div>
	);
};
export default SystemStatus;
