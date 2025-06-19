import AgentErrors from "@/components/management-panel/agent-errors";
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
	getAgentErrors,
	getIncomingMessages,
	getOutgoingMessages,
	useStore,
	type Agent,
} from "@/hooks/use-store";
import { cn, parseTimestamp } from "@/lib/utils";
import {
	AlertTriangle,
	Cpu,
	Inbox,
	MoveDown,
	MoveUp,
	Pause,
	PenLine,
	Play,
	Radio,
	Send,
	Shield,
	TriangleAlert,
	Zap,
	ZapOff,
} from "lucide-react";
import { useCallback } from "react";

const AgentCard = ({ agent }: { agent: Agent }) => {
	const { messages, errors } = useStore();

	const incomingMessages = getIncomingMessages(agent.aid, messages);
	const outgoingMessages = getOutgoingMessages(agent.aid, messages);
	const agentErrors = getAgentErrors(agent.aid, errors);

	const getLastMessage = useCallback(() => {
		return incomingMessages.sort(
			(a, b) =>
				parseTimestamp(b.timestamp).getTime() -
				parseTimestamp(a.timestamp).getTime()
		)[0];
	}, [incomingMessages]);

	const getAgentStatusIndicatorColor = useCallback(() => {
		if (!agent.status.isEnabled) return "bg-red-500";
		if (!agent.status.isWorking) return "bg-blue-500";
		return "bg-green-500";
	}, [agent.status.isEnabled, agent.status.isWorking]);

	const getAgentStatusNameColor = useCallback(() => {
		if (!agent.status.isEnabled) return "text-red-500";
		if (!agent.status.isWorking) return "text-blue-500";
		return "text-green-500";
	}, [agent.status.isEnabled, agent.status.isWorking]);

	const getAgentStatusBadgeColor = useCallback(() => {
		if (!agent.status.isEnabled)
			return "bg-red-500/20 text-red-800 dark:text-red-400 dark:border-red-400";
		if (!agent.status.isWorking)
			return "bg-blue-500/20 text-blue-800 dark:text-blue-400 dark:border-blue-400";
		return "bg-green-500/20 text-green-800 dark:text-green-400 dark:border-green-400";
	}, [agent.status.isEnabled, agent.status.isWorking]);

	const getAgentStatusText = useCallback(() => {
		if (!agent.status.isEnabled) return "Disabled";
		if (!agent.status.isWorking) return "Idle";
		return "Working";
	}, [agent.status.isEnabled, agent.status.isWorking]);

	const getCurrentPowerDraw = useCallback(() => {
		if (agent.status.isWorking) {
			return agent.status.activeDraw || 0;
		}
		return agent.status.idleDraw || 0;
	}, [
		agent.status.activeDraw,
		agent.status.idleDraw,
		agent.status.isWorking,
	]);

	return (
		<Card className="hover:shadow-lg transition-shadow w-full max-w-md">
			<CardHeader>
				<div className="flex items-center justify-between">
					<div
						className={cn(
							"flex items-center gap-2",
							agent.type === "appliance"
								? getAgentStatusNameColor()
								: "text-primary"
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
							{getAgentStatusText()}
						</Badge>
					</div>
				</div>
				<CardDescription>
					<div className="flex justify-between items-center mb-2">
						<span className="text-sm text-muted-foreground">
							Messages
						</span>
						<div className="flex gap-2">
							<Badge variant="outline" className="text-xs">
								<MoveDown /> {incomingMessages.length}
							</Badge>
							<Badge variant="outline" className="text-xs">
								<MoveUp /> {outgoingMessages.length}
							</Badge>
							{agentErrors.length > 0 && (
								<Badge
									variant="outline"
									className="text-xs text-red-600"
								>
									<TriangleAlert />
									{agentErrors.length}
								</Badge>
							)}
						</div>
					</div>

					{/* Power and Task Info */}
					<div className="flex flex-wrap gap-2 text-xs">
						<div className="flex items-center gap-1">
							{getCurrentPowerDraw() > 0 ? (
								<Zap className="size-4 text-yellow-500" />
							) : (
								<ZapOff className="size-4 text-muted-foreground" />
							)}
							<span>{getCurrentPowerDraw()}W</span>
						</div>

						{agent.status.priority !== undefined && (
							<div className="flex items-center gap-1">
								<Shield className="size-4 text-blue-500" />
								<span>P{agent.status.priority}</span>
							</div>
						)}

						{agent.status.isTaskInterruptible !== undefined && (
							<div className="flex items-center gap-1">
								{agent.status.isTaskInterruptible ? (
									<Pause className="size-4 text-orange-500" />
								) : (
									<Play className="size-4 text-green-500" />
								)}
								<span>
									{agent.status.isTaskInterruptible
										? "Interruptible"
										: "Non-interruptible"}
								</span>
							</div>
						)}
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
							<DialogTitle className="flex items-center gap-2">
								{agent.name}
								<div className="flex gap-2">
									<Badge
										variant="outline"
										className="text-xs"
									>
										Priority:{" "}
										{agent.status.priority || "N/A"}
									</Badge>
									<Badge
										variant="outline"
										className="text-xs"
									>
										{getCurrentPowerDraw()}W
									</Badge>
								</div>
							</DialogTitle>
							<div className="space-y-2">
								<DialogDescription>
									Manage agent communications - view
									incoming/outgoing messages and compose new
									ones.
								</DialogDescription>
								{agent.status.isTaskInterruptible !==
									undefined && (
									<div className="text-xs text-muted-foreground">
										Task:{" "}
										{agent.status.isTaskInterruptible
											? "Interruptible"
											: "Non-interruptible"}
										{agent.status.isTaskResumable &&
											" & Resumable"}
									</div>
								)}
							</div>
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
								<TabsTrigger value="errors">
									<AlertTriangle className="size-4" />
									Errors ({agentErrors.length})
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
							<TabsContent value="errors">
								<AgentErrors errors={agentErrors} />
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
