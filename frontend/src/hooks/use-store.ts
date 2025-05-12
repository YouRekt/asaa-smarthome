import type { Agent } from "@/components/agent";
import { create } from "zustand";

export type Environment = {
	time: string;
	credits: number;
	timeDelta: number;
	maxPowerCapacity: number;
	currentPowerConsumption: number;
	areas: {
		name: string;
		attributes: Record<string, string | number | boolean>;
	}[];
};

export type Agent = {
	aid: string;
	name: string;
	area: string;
};

type State = {
	agents: Agent[];
	environment: Environment | null;
	selectedRoom: string | null;
	isRunning: boolean;
	agentMessages: Record<
		string,
		{
			message: string;
			timestamp: string;
		}
	>;
	errors: Record<
		string,
		Array<{
			message: string;
			timestamp: string;
		}>
	>;
};

type Actions = {
	setAgents: (agents: Agent[]) => void;
	addAgent: (agent: Agent) => void;
	setEnvironment: (environment: Environment) => void;
	setSelectedRoom: (selectedRoom: string) => void;
	setIsRunning: (isRunning: boolean) => void;
	setAgentMessage: (
		agentId: string,
		timestamp: string,
		message: string
	) => void;
	setError: (aid: string, timestamp: string, message: string) => void;
};

const useStore = create<State & Actions>((set) => ({
	agents: [],
	environment: null,
	selectedRoom: null,
	isRunning: false,
	agentMessages: {},
	errors: {},
	setAgents: (agents) => set({ agents }),
	addAgent: (agent) => set((state) => ({ agents: [...state.agents, agent] })),
	setEnvironment: (environment) => set({ environment }),
	setSelectedRoom: (selectedRoom) => set({ selectedRoom }),
	setIsRunning: (isRunning) => set({ isRunning }),
	setAgentMessage: (agentId: string, timestamp: string, message: string) =>
		set((state) => ({
			agentMessages: {
				...state.agentMessages,
				[agentId]: { message, timestamp },
			},
		})),
	setError: (aid: string, timestamp: string, message: string) =>
		set((state) => ({
			errors: {
				...state.errors,
				[aid]: [...state.errors[aid], { message, timestamp }],
			},
		})),
}));

export default useStore;
