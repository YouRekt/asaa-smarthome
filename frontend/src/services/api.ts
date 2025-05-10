const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export async function fetchAgentsByRoom(roomId: any) {
  const res = await fetch(`${API_BASE}/rooms/${roomId}/agents`);
  if (!res.ok) throw new Error('Failed to fetch agents');
  return res.json();
}
