import Message from "@/components/management-panel/message";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useStore } from "@/hooks/use-store";
import { useEffect, useRef } from "react";

const Messages = () => {
	const { areas, selectedArea, messages, agents } = useStore();

	const currentRoom = areas.find((area) => area.name === selectedArea);

	const currentRoomMessages = messages.filter(
		(message) =>
			agents.find((agent) => agent.aid === message.aid)?.area ===
			selectedArea
	);

	const scrollRef = useRef<HTMLDivElement>(null);
	useEffect(() => {
		scrollRef.current?.scrollIntoView({
			behavior: "smooth",
		});
	}, [currentRoomMessages]);

	return (
		<Card>
			<CardHeader>
				<CardTitle>Recent Messages</CardTitle>
				<CardDescription>
					Messages from agents in {currentRoom?.name}
				</CardDescription>
			</CardHeader>
			<CardContent>
				<ScrollArea className="h-96">
					<div className="space-y-3">
						{currentRoomMessages.length === 0 ? (
							<p className="text-center text-muted-foreground py-8">
								No messages yet
							</p>
						) : (
							currentRoomMessages.map((message) => {
								const agent = agents.find(
									(agent) => message.aid === agent.aid
								);
								return (
									<Message
										key={message.aid + message.timestamp}
										message={message}
										agent={agent}
									/>
								);
							})
						)}
						<div ref={scrollRef} />
					</div>
				</ScrollArea>
			</CardContent>
		</Card>
	);
};
export default Messages;
