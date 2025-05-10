import { ReactElement } from 'react';

export default function HouseMap({ onRoomClick }) {
  const handleClick = (event) => {
    const roomId = event.target.id;
    if (roomId) onRoomClick(roomId);
  };

  return (
    <div onClick={handleClick} className="cursor-pointer">
      <img src="/house.svg" alt="House schematic" />
    </div>
  );
}