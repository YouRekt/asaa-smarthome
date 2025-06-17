import { Badge } from "@/components/ui/badge";
import type { Agent, Message as MessageType } from "@/hooks/use-store";
import { cn, formatDate } from "@/lib/utils";

const Message = ({
	message,
	agent,
}: {
	message: MessageType;
	agent?: Agent;
}) => {
	return (
		<div className="p-3 mr-4 rounded-lg border bg-accent">
			{agent ? (
				<div className="flex items-center justify-between mb-1">
					<span className="font-medium text-sm">
						{agent?.name}
						<Badge
							className={cn(
								"ml-2 text-xs",
								agent?.type === "sensor"
									? "bg-blue-500/20 text-blue-800 dark:text-blue-400 dark:border-blue-400"
									: "bg-green-500/20 text-green-800 dark:text-green-400 dark:border-green-400"
							)}
						>
							{agent?.type
								? agent.type.charAt(0).toUpperCase() +
								  agent.type.slice(1)
								: ""}
						</Badge>
					</span>

					<span className="text-xs text-muted-foreground">
						{formatDate(message.timestamp)}
					</span>
				</div>
			) : (
				<div className="flex items-center justify-between mb-1">
					<span className="text-xs font-semibold text-muted-foreground">
						{formatDate(message.timestamp)}
					</span>
				</div>
			)}
			<p className="text-sm text-muted-foreground">{message.content}</p>
		</div>
	);
};
export default Message;
