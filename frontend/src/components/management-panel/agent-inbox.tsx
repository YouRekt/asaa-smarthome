import Message from "@/components/management-panel/message";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useStore, type Agent } from "@/hooks/use-store";
import { useEffect, useRef } from "react";

const AgentInbox = ({ agent }: { agent: Agent }) => {
	const { messages } = useStore();

	const agentMessages = messages.filter(
		(message) => message.aid === agent.aid
	);

	const scrollRef = useRef<HTMLDivElement>(null);
	useEffect(() => {
		scrollRef.current?.scrollIntoView({
			behavior: "smooth",
		});
	}, [agentMessages]);

	return (
		<ScrollArea className="h-96">
			<div className="space-y-3">
				{agentMessages.length === 0 ? (
					<p className="text-center text-muted-foreground py-8">
						No messages yet
					</p>
				) : (
					agentMessages.map((message) => (
						<Message
							key={message.aid + message.timestamp}
							message={message}
						/>
					))
				)}
				<div ref={scrollRef} />
			</div>
		</ScrollArea>
	);
};
export default AgentInbox;
