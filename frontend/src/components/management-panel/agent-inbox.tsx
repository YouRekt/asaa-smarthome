import Message from "@/components/management-panel/message";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { Agent, Message as MessageType } from "@/hooks/use-store";
import { parseTimestamp } from "@/lib/utils";
import { Inbox } from "lucide-react";

const AgentInbox = ({
	agent,
	messages,
}: {
	agent: Agent;
	messages: MessageType[];
}) => {
	if (messages.length === 0) {
		return (
			<div className="flex flex-col items-center justify-center h-64 text-muted-foreground">
				<Inbox className="h-12 w-12 mb-4 opacity-50" />
				<p className="text-lg font-medium">No incoming messages</p>
				<p className="text-sm">
					This agent hasn't received any messages yet.
				</p>
			</div>
		);
	}

	return (
		<ScrollArea className="h-96">
			<div className="flex flex-col gap-4 p-4">
				{messages
					.sort(
						(a, b) =>
							parseTimestamp(b.timestamp).getTime() -
							parseTimestamp(a.timestamp).getTime()
					)
					.map((message, index) => (
						<Message
							key={index}
							message={message}
							viewerAgent={agent}
							showDirection={true}
						/>
					))}
			</div>
		</ScrollArea>
	);
};

export default AgentInbox;
