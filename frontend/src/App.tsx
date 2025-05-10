import { useRef, useState } from "react";
import HouseMap from "@/components/HouseMap";
import EnvironmentViewer from "@/components/EnvironmentViewer";

export default function App() {
	const [selectedRoom, setSelectedRoom] = useState<number | null>(null);

	const floorPlanContainerRef = useRef<HTMLDivElement>(null);

	const handleRoomClick = async (roomId: number) => {
		setSelectedRoom(roomId);
	};

	return (
		<div className="flex h-screen">
			<div className="flex-1 p-4 relative" ref={floorPlanContainerRef}>
				<HouseMap onRoomClick={handleRoomClick} />
			</div>
			<div className="w-1/3 border-l p-4">
				<h2 className="text-xl font-semibold mb-2">
					{selectedRoom
						? `Agents in ${selectedRoom}`
						: "Select a room"}
				</h2>
				<EnvironmentViewer />
			</div>
		</div>
	);
}
