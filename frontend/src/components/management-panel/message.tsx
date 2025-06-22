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

	// Check if content is JSON and format accordingly
	const formatContent = (content: string) => {
		if (!content) return null;

		try {
			// Try to parse as JSON
			const jsonContent = JSON.parse(content);
			return {
				isJson: true,
				content: JSON.stringify(jsonContent, null, 2),
			};
		} catch {
			// If not JSON, return as plain text
			return {
				isJson: false,
				content: content,
			};
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

	const formattedContent = message.content
		? formatContent(message.content)
		: null;

	return (
		<div
			className={cn(
				"p-3 rounded-lg border bg-accent max-w-full w-full overflow-hidden",
				showDirection && "border-l-4",
				getBorderColor()
			)}
		>
			{/* Header with agent info or direction indicators */}
			<div className="flex items-center justify-between mb-2 min-w-0">
				<div className="flex items-center gap-2 flex-wrap min-w-0 flex-1 overflow-hidden">
					{/* Direction indicator for agent-specific views */}
					{showDirection && viewerAgent && (
						<div className="flex items-center gap-1 flex-shrink-0">
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
							<span className="font-medium text-sm truncate">
								{agent.name}
							</span>
							<Badge
								className={cn(
									"text-xs flex-shrink-0",
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
						<div className="flex items-center gap-2 text-sm min-w-0 flex-1">
							{isOutgoing ? (
								<>
									<span className="font-medium flex-shrink-0">
										To:
									</span>
									<div className="flex gap-1 flex-wrap min-w-0">
										{message.receiver.map(
											(receiver, idx) => (
												<Badge
													key={idx}
													variant="outline"
													className="text-xs"
													title={receiver}
												>
													{receiver}
												</Badge>
											)
										)}
									</div>
								</>
							) : (
								<>
									<span className="font-medium flex-shrink-0">
										From:
									</span>
									<Badge
										variant="outline"
										className="text-xs"
										title={message.sender}
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
							"text-xs flex-shrink-0",
							getPerformativeColor(message.performative)
						)}
					>
						{message.performative}
					</Badge>
				</div>

				{/* Timestamp */}
				<span className="text-xs text-muted-foreground flex-shrink-0 ml-2">
					{message.timestamp}
				</span>
			</div>

			{/* Conversation ID */}
			<div
				className="text-xs text-muted-foreground mb-2 truncate"
				title={message.conversationId}
			>
				Conversation: {message.conversationId}
			</div>

			{/* Message content - conditional formatting */}
			<div className="text-sm w-full overflow-hidden">
				{formattedContent ? (
					formattedContent.isJson ? (
						<pre className="whitespace-pre-wrap break-words font-mono text-xs bg-muted/50 p-2 rounded border overflow-x-auto">
							{formattedContent.content}
						</pre>
					) : (
						<p className="break-words whitespace-pre-wrap">
							{formattedContent.content}
						</p>
					)
				) : (
					<em className="text-muted-foreground">No content</em>
				)}
			</div>
		</div>
	);
};

export default Message;
