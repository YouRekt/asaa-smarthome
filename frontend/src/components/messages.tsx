import useStore from "@/hooks/use-store";
export const Messages = () => {
	const { selectedAgent, agentMessages } = useStore();
	return (
		<div className="flex flex-col gap-2 overflow-y-auto max-h-1/2">
			<h2 className="text-xl font-semibold mb-2">
				{selectedAgent ? `${selectedAgent}` : "Select an agent"}
			</h2>
			<div className="flex flex-col gap-2">
				{selectedAgent &&
					agentMessages[selectedAgent]?.map((message) => (
						<div
							key={
								selectedAgent +
								message.timestamp +
								message.message
							}
						>
							[{message.timestamp}] : {message.message}
						</div>
					))}
			</div>
		</div>
	);
};
