import useStore from "@/hooks/use-store";

const EnvironmentViewer = () => {
	const { environment, selectedRoom } = useStore();

	if (!environment) return <p>Waiting for environment data...</p>;

	return (
		<div>
			<h2>Environment Status</h2>
			<p>
				<strong>Time:</strong> {environment.time}
			</p>
			<p>
				<strong>credits:</strong> {environment.credits}
			</p>
			<p>
				<strong>Delta:</strong> {environment.timeDelta}
			</p>

			{selectedRoom && (
				<>
					<h3>Area Status</h3>
					<ul>
						{environment.areas
							.filter((area) => area.name === selectedRoom)
							.map((area) => (
								<li key={area.name}>
									<strong>{area.name}:</strong>
									<ul>
										{Object.entries(area.attributes).map(
											([key, value]) => (
												<li key={key}>
													{key}:{" "}
													{typeof value === "number"
														? value.toFixed(2) +
														  " °C"
														: JSON.stringify(value)}
												</li>
											)
										)}
									</ul>
								</li>
							))}
					</ul>
				</>
			)}
		</div>
	);
};

export default EnvironmentViewer;
