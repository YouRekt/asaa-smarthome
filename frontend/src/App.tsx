import { useState } from 'react';
import { fetchAgentsByRoom } from './services/api';
import HouseMap from '@/components/HouseMap';
import AgentList from '@/components/AgentList';

export default function App() {
  const [selectedRoom, setSelectedRoom] = useState(null);
  const [agents, setAgents] = useState([]);

  const handleRoomClick = async (roomId: any) => {
    setSelectedRoom(roomId);
    const list = await fetchAgentsByRoom(roomId);
    setAgents(list);
  };

  return (
    <div className="flex h-screen">
      <div className="flex-1 p-4">
        <HouseMap onRoomClick={handleRoomClick} />
      </div>
      <div className="w-1/3 border-l p-4">
        <h2 className="text-xl font-semibold mb-2">
          {selectedRoom ? `Agents in ${selectedRoom}` : 'Select a room'}
        </h2>
        <AgentList agents={agents} />
      </div>
    </div>
  );
}