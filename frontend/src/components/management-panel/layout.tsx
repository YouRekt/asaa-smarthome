import ConfigPanel from "@/components/config-panel/config-panel";
import Areas from "@/components/management-panel/areas";
import Header from "@/components/management-panel/header";
import Sidebar from "@/components/management-panel/sidebar";
import SystemStatus from "@/components/management-panel/system-status";
import { useStore } from "@/hooks/use-store";
import { Loader2 } from "lucide-react";

const Layout = ({ children }: React.PropsWithChildren) => {
	const { systemStatus } = useStore();

	return (
		<div className="flex h-dvh bg-background">
			<Sidebar>
				<SystemStatus />
				{systemStatus === "running" && <Areas />}
			</Sidebar>
			{systemStatus === "running" ? (
				<div className="flex-1 flex flex-col overflow-hidden">
					<Header />
					{children}
				</div>
			) : systemStatus === "stopped" ? (
				<ConfigPanel />
			) : (
				<main className="flex-1 p-4 overflow-y-auto">
					<div className="flex items-center justify-center h-full">
						<p className="text-muted-foreground">
							The system is {systemStatus}. Please wait...
						</p>
						<Loader2 className="animate-spin size-6 ml-2 text-muted-foreground" />
					</div>
				</main>
			)}
		</div>
	);
};
export default Layout;
