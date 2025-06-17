import AgentInbox from "@/components/management-panel/agent-inbox";
import AgentMessageForm from "@/components/management-panel/agent-message-form";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardFooter,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useStore, type Agent } from "@/hooks/use-store";
import { cn } from "@/lib/utils";
import { Cpu, Inbox, PenLine, Radio } from "lucide-react";
import { useCallback } from "react";

const AgentCard = ({ agent }: { agent: Agent }) => {
	const { messages } = useStore();

	const agentMessages = messages.filter(
		(message) => message.aid === agent.aid
	);

	const getAgentStatusIndicatorColor = useCallback(() => {
		switch (agent.status) {
			default:
			case "disabled":
				return "bg-red-500";
			case "enabled":
				return "bg-blue-500";
			case "working":
				return "bg-green-500";
		}
	}, [agent.status]);

	const getAgentStatusBadgeColor = useCallback(() => {
		switch (agent.status) {
			default:
			case "disabled":
				return "bg-red-500/20 text-red-800 dark:text-red-400 dark:border-red-400";
			case "enabled":
				return "bg-blue-500/20 text-blue-800 dark:text-blue-400 dark:border-blue-400";
			case "working":
				return "bg-green-500/20 text-green-800 dark:text-green-400 dark:border-green-400";
		}
	}, [agent.status]);

	return (
		<Card className="hover:shadow-lg transition-shadow w-full max-w-md">
			<CardHeader>
				<div className="flex items-center justify-between">
					<div
						className={cn(
							"flex items-center gap-2",
							agent.type === "appliance"
								? "text-blue-600"
								: "text-green-600"
						)}
					>
						{agent.type === "appliance" ? <Cpu /> : <Radio />}
						<CardTitle className="text-lg">{agent.name}</CardTitle>
					</div>
					<div className="flex items-center gap-2">
						<div
							className={cn(
								"size-2 rounded-full",
								getAgentStatusIndicatorColor()
							)}
						/>

						<Badge
							className={cn(
								"ml-2 text-xs",
								getAgentStatusBadgeColor()
							)}
						>
							{agent.status.charAt(0).toUpperCase() +
								agent.status.slice(1)}
						</Badge>
					</div>
				</div>
				<CardDescription>
					<div className="flex justify-between">
						<span className="text-sm text-muted-foreground items-baseline">
							Messages
						</span>
						<Badge className="ml-2" variant="outline">
							{agentMessages.length}
						</Badge>
					</div>
				</CardDescription>
			</CardHeader>
			<CardContent className="text-sm text-muted-foreground truncate">
				<Badge variant="outline">Last message</Badge>
				<span className="ml-2">
					{
						agentMessages.sort(
							(a, b) =>
								new Date(b.timestamp).getTime() -
								new Date(a.timestamp).getTime()
						)[0]?.content
					}
				</span>
			</CardContent>
			<CardFooter className="flex justify-between">
				<Dialog>
					<DialogTrigger asChild>
						<Button variant="outline">View</Button>
					</DialogTrigger>
					<DialogContent className="max-h-dvh max-w-dvw">
						<DialogHeader>
							<DialogTitle>{agent.name}</DialogTitle>
							<DialogDescription>
								Here you can view agents messages as well as
								send them.
							</DialogDescription>
						</DialogHeader>
						<Tabs defaultValue="inbox">
							<TabsList>
								<TabsTrigger value="inbox">
									<Inbox /> Inbox
								</TabsTrigger>
								<TabsTrigger value="compose">
									<PenLine /> Compose
								</TabsTrigger>
							</TabsList>
							<TabsContent value="inbox">
								<AgentInbox agent={agent} />
							</TabsContent>
							<TabsContent value="compose">
								<AgentMessageForm agent={agent} />
							</TabsContent>
						</Tabs>
					</DialogContent>
				</Dialog>
			</CardFooter>
		</Card>
	);
};
export default AgentCard;
