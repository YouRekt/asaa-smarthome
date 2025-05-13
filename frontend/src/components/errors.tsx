import useStore from "@/hooks/use-store";

export const Errors = () => {
	const { errors } = useStore();

	return (
		<div className="flex flex-col gap-2 overflow-y-auto max-h-1/2">
			<h2 className="text-xl font-semibold mb-2">Errors</h2>
			{Object.entries(errors).map(([aid, messages]) => (
				<div key={aid}>
					<h3>{aid}</h3>
					{messages.map((message) => (
						<p
							className="text-red-600"
							key={aid + message.timestamp}
						>
							{message.message}
						</p>
					))}
				</div>
			))}
		</div>
	);
};
