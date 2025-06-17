import AgentInbox from "@/components/management-panel/agent-inbox";
import AgentMessageForm from "@/components/management-panel/agent-message-form";
import AgentOutbox from "@/components/management-panel/agent-outbox";
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
import {
	getIncomingMessages,
	getOutgoingMessages,
	useStore,
	type Agent,
} from "@/hooks/use-store";
import { cn } from "@/lib/utils";
import { Cpu, Inbox, PenLine, Radio, Send } from "lucide-react";
import { useCallback } from "react";

const AgentCard = ({ agent }: { agent: Agent }) => {
	const { messages } = useStore();

	const incomingMessages = getIncomingMessages(agent.aid, messages);
	const outgoingMessages = getOutgoingMessages(agent.aid, messages);

	const getLastMessage = () => {
		const allAgentMessages = [...incomingMessages, ...outgoingMessages];
		return allAgentMessages.sort(
			(a, b) =>
				new Date(b.timestamp).getTime() -
				new Date(a.timestamp).getTime()
		)[0];
	};

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
					<div className="flex justify-between items-center">
						<span className="text-sm text-muted-foreground">
							Messages
						</span>
						<div className="flex gap-2">
							<Badge variant="outline" className="text-xs">
								↓ {incomingMessages.length}
							</Badge>
							<Badge variant="outline" className="text-xs">
								↑ {outgoingMessages.length}
							</Badge>
						</div>
					</div>
				</CardDescription>
			</CardHeader>
			<CardContent className="text-sm text-muted-foreground">
				<div className="flex items-center gap-2 mb-2">
					<Badge variant="outline">Last message</Badge>
				</div>
				{getLastMessage() ? (
					<div className="text-xs">
						<div className="flex gap-2 items-center mb-1">
							<span className="font-medium">
								{getLastMessage()?.performative}
							</span>
							<span className="text-muted-foreground">
								{getLastMessage()?.conversationId}
							</span>
						</div>
						<p className="truncate">{getLastMessage()?.content}</p>
					</div>
				) : (
					<span className="text-xs">No messages</span>
				)}
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
								Manage agent communications - view
								incoming/outgoing messages and compose new ones.
							</DialogDescription>
						</DialogHeader>
						<Tabs defaultValue="inbox">
							<TabsList>
								<TabsTrigger value="inbox">
									<Inbox className="size-4" />
									Inbox ({incomingMessages.length})
								</TabsTrigger>
								<TabsTrigger value="outbox">
									<Send className="size-4" />
									Outbox ({outgoingMessages.length})
								</TabsTrigger>
								<TabsTrigger value="compose">
									<PenLine className="size-4" />
									Compose
								</TabsTrigger>
							</TabsList>
							<TabsContent value="inbox">
								<AgentInbox
									agent={agent}
									messages={incomingMessages}
								/>
							</TabsContent>
							<TabsContent value="outbox">
								<AgentOutbox
									agent={agent}
									messages={outgoingMessages}
								/>
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
