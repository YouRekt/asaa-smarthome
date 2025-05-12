import useStore from "@/hooks/use-store";

export const Errors = () => {
	const { errors } = useStore();

	return (
		<>
			{Object.entries(errors).map(([aid, messages]) => (
				<div key={aid}>
					<h3>{aid}</h3>
					{messages.map((message) => (
						<p key={message.timestamp}>{message.message}</p>
					))}
				</div>
			))}
		</>
	);
};
