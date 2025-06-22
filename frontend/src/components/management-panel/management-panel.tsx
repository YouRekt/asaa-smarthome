import Agents from "@/components/management-panel/agents";
import Messages from "@/components/management-panel/messages";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { MessageCircle, Users } from "lucide-react";

const ManagementPanel = () => {
	return (
		<main className="flex-1 p-4 overflow-y-auto">
			<Tabs defaultValue="agents">
				<TabsList>
					<TabsTrigger value="agents">
						<Users />
						Agents
					</TabsTrigger>
					<TabsTrigger value="messages">
						<MessageCircle />
						Messages
					</TabsTrigger>
				</TabsList>
				<TabsContent value="agents">
					<Agents />
				</TabsContent>
				<TabsContent value="messages">
					<Messages />
				</TabsContent>
			</Tabs>
		</main>
	);
};
export default ManagementPanel;
