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

	// Filter messages to show all messages where sender or any receiver is in the current room
	const currentRoomMessages = messages.filter((message) => {
		const senderAgent = agents.find(
			(agent) => agent.aid === message.sender
		);
		const receiverAgents = message.receiver
			.map((receiverId) =>
				agents.find((agent) => agent.aid === receiverId)
			)
			.filter(Boolean);

		return (
			senderAgent?.area === selectedArea ||
			receiverAgents.some((agent) => agent?.area === selectedArea)
		);
	});

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
					<div className="flex flex-col gap-3">
						{currentRoomMessages.length === 0 ? (
							<p className="text-center text-muted-foreground py-8">
								No messages yet
							</p>
						) : (
							currentRoomMessages.map((message, index) => {
								const senderAgent = agents.find(
									(agent) => message.sender === agent.aid
								);
								return (
									<Message
										key={`${message.sender}-${message.timestamp}-${index}`}
										message={message}
										agent={senderAgent}
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
