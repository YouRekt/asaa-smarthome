import { House } from '@/components/House';
import { ReactElement } from 'react';

export default function HouseMap({ onRoomClick }) {
  const handleClick = (event) => {
    const roomId = event.target.id;
    if (roomId) onRoomClick(roomId);
  };

  return (
    <House onClick={handleClick}/>
  );
}