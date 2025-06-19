import { Badge } from "@/components/ui/badge";
import type {
	Agent,
	Message as MessageType,
	Performative,
} from "@/hooks/use-store";
import { cn } from "@/lib/utils";
import { ArrowDown, ArrowUp } from "lucide-react";

type MessageProps = {
	message: MessageType;
	agent?: Agent; // For room view - shows agent info
	viewerAgent?: Agent; // For agent-specific views - determines direction
	showDirection?: boolean; // Whether to show incoming/outgoing indicators
};

const Message = ({
	message,
	agent,
	viewerAgent,
	showDirection = false,
}: MessageProps) => {
	const getPerformativeColor = (performative: Performative) => {
		switch (performative) {
			case "INFORM":
				return "bg-blue-600/10 text-blue-800 dark:text-blue-600";
			case "REQUEST":
				return "bg-orange-600/10 text-orange-800 dark:text-orange-600";
			case "CFP":
				return "bg-purple-600/10 text-purple-800 dark:text-purple-600";
			case "AGREE":
				return "bg-green-600/10 text-green-800 dark:text-green-600";
			case "REFUSE":
				return "bg-red-600/10 text-red-800 dark:text-red-600";
			case "CONFIRM":
				return "bg-teal-600/10 text-teal-800 dark:text-teal-600";
			case "REJECT_PROPOSAL":
				return "bg-rose-600/10 text-rose-800 dark:text-rose-600";
			case "ACCEPT_PROPOSAL":
				return "bg-emerald-600/10 text-emerald-800 dark:text-emerald-600";
			case "PROPOSE":
				return "bg-yellow-600/10 text-yellow-800 dark:text-yellow-600";
			default:
				return "bg-zinc-500/10 text-zinc-800 dark:text-zinc-600";
		}
	};

	// Use the outgoing flag from the message
	const isOutgoing = message.outgoing;
	const isIncoming = !message.outgoing;

	// Get border color based on message direction
	const getBorderColor = () => {
		if (!showDirection || !viewerAgent) return "";
		if (isOutgoing) return "border-l-orange-500";
		if (isIncoming) return "border-l-green-500";
		return "";
	};

	return (
		<div
			className={cn(
				"p-3 mr-4 rounded-lg border bg-accent",
				showDirection && "border-l-4",
				getBorderColor()
			)}
		>
			{/* Header with agent info or direction indicators */}
			<div className="flex items-center justify-between mb-2">
				<div className="flex items-center gap-2 flex-wrap">
					{/* Direction indicator for agent-specific views */}
					{showDirection && viewerAgent && (
						<div className="flex items-center gap-1">
							{isOutgoing && (
								<ArrowUp className="h-4 w-4 text-orange-500" />
							)}
							{isIncoming && (
								<ArrowDown className="h-4 w-4 text-green-500" />
							)}
						</div>
					)}

					{/* Agent info for room view */}
					{agent && !showDirection && (
						<>
							<span className="font-medium text-sm">
								{agent.name}
							</span>
							<Badge
								className={cn(
									"text-xs",
									agent.type === "sensor"
										? "bg-blue-500/20 text-blue-800 dark:text-blue-400 dark:border-blue-400"
										: "bg-green-500/20 text-green-800 dark:text-green-400 dark:border-green-400"
								)}
							>
								{agent.type.charAt(0).toUpperCase() +
									agent.type.slice(1)}
							</Badge>
						</>
					)}

					{/* Sender/Receiver info for agent-specific views */}
					{showDirection && viewerAgent && (
						<div className="flex items-center gap-2 text-sm">
							{isOutgoing ? (
								<>
									<span className="font-medium">To:</span>
									<div className="flex gap-1 flex-wrap">
										{message.receiver.map(
											(receiver, idx) => (
												<Badge
													key={idx}
													variant="outline"
													className="text-xs"
												>
													{receiver}
												</Badge>
											)
										)}
									</div>
								</>
							) : (
								<>
									<span className="font-medium">From:</span>
									<Badge
										variant="outline"
										className="text-xs"
									>
										{message.sender}
									</Badge>
								</>
							)}
						</div>
					)}

					{/* Performative badge */}
					<Badge
						className={cn(
							"text-xs",
							getPerformativeColor(message.performative)
						)}
					>
						{message.performative}
					</Badge>
				</div>

				{/* Timestamp */}
				<span className="text-xs text-muted-foreground">
					{message.timestamp}
				</span>
			</div>

			{/* Conversation ID */}
			<div className="text-xs text-muted-foreground mb-2">
				Conversation: {message.conversationId}
			</div>

			{/* Message content - handle empty string */}
			<p className="text-sm">
				{message.content || (
					<em className="text-muted-foreground">No content</em>
				)}
			</p>
		</div>
	);
};

export default Message;
