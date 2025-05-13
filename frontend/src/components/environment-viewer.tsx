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
				<strong>Error rate:</strong>{" "}
				{(
					(Number.isNaN(
						environment.errorTasks / environment.performedTasks
					)
						? 0
						: environment.errorTasks / environment.performedTasks) *
					100
				).toFixed(2)}
				%
			</p>
			<p>
				<strong>Credits:</strong> {environment.credits}
			</p>
			<p>
				<strong>Time Delta:</strong> {environment.timeDelta}
			</p>
			<p>
				<strong>Human Location:</strong> {environment.humanLocation}
			</p>
			<p>
				<strong>Max Power Capacity:</strong>{" "}
				{environment.maxPowerCapacity}
			</p>
			<p>
				<strong>Current Power Consumption:</strong>{" "}
				{environment.currentPowerConsumption}
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
