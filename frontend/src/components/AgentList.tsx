export default function AgentList({ agents }) {
  if (!agents.length) return <div>No agents</div>;
  return (
    <ul className="list-disc pl-5">
      {agents.map((agent) => (
        <li key={agent}>{agent}</li>
      ))}
    </ul>
  );
}