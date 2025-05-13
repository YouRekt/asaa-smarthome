import type { Agent } from "@/components/agent";
import { create } from "zustand";

export type Environment = {
	time: string;
	credits: number;
	timeDelta: number;
	maxPowerCapacity: number;
	currentPowerConsumption: number;
	humanLocation: string;
	performedTasks: number;
	errorTasks: number;
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

export type AgentStatus = {
	aid: string;
	isEnabled: boolean;
	isWorking: boolean;
	isInterruptible: boolean;
	isFreezable: boolean;
	activeDraw: number;
	idleDraw: number;
	priority: number;
};

export type StoredAgentStatus = Omit<AgentStatus, "aid">;

type State = {
	agents: Agent[];
	agentStatus: Record<string, StoredAgentStatus>;
	environment: Environment | null;
	selectedRoom: string | null;
	selectedAgent: string | null;
	isRunning: boolean;
	agentMessages: Record<
		string,
		Array<{
			message: string;
			timestamp: string;
		}>
	>;
	errors: Record<
		string,
		Array<{
			message: string;
			timestamp: string;
		}>
	>;
	showErrors: boolean;
	modalOpen: string | null;
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
	setSelectedAgent: (aid: string) => void;
	deselectAgent: () => void;
	setShowErrors: (showErrors: boolean) => void;
	setModalOpen: (modalOwner: string | null) => void;
	setAgentStatus: (status: AgentStatus) => void;
	clearAll: () => void;
};

const useStore = create<State & Actions>((set) => ({
	agents: [],
	environment: null,
	selectedRoom: null,
	isRunning: false,
	agentMessages: {},
	errors: {},
	showErrors: false,
	modalOpen: null,
	selectedAgent: null,
	agentStatus: {},
	setAgents: (agents) => set({ agents }),
	addAgent: (agent) => set((state) => ({ agents: [...state.agents, agent] })),
	setEnvironment: (environment) => set({ environment }),
	setSelectedRoom: (selectedRoom) => set({ selectedRoom }),
	setIsRunning: (isRunning) => set({ isRunning }),
	setAgentMessage: (agentId: string, timestamp: string, message: string) =>
		set((state) => ({
			agentMessages: {
				...state.agentMessages,
				[agentId]: [
					...(state.agentMessages[agentId] || []),
					{ message, timestamp },
				],
			},
		})),
	setError: (aid: string, timestamp: string, message: string) =>
		set((state) => ({
			errors: {
				...state.errors,
				[aid]: [...(state.errors[aid] || []), { message, timestamp }],
			},
		})),
	setSelectedAgent: (aid) => set({ selectedAgent: aid }),
	deselectAgent: () => set({ selectedAgent: null }),
	setShowErrors: (showErrors) => set({ showErrors }),
	setModalOpen: (modalOwner) => set({ modalOpen: modalOwner }),
	setAgentStatus: (status: AgentStatus) =>
		set((state) => {
			const { aid, ...statusWithoutAid } = status;
			return {
				agentStatus: {
					...state.agentStatus,
					[aid]: statusWithoutAid,
				},
			};
		}),
	clearAll: () =>
		set({
			agents: [],
			environment: null,
			selectedRoom: null,
			isRunning: false,
			agentMessages: {},
			errors: {},
			showErrors: false,
			modalOpen: null,
			selectedAgent: null,
			agentStatus: {},
		}),
}));

export default useStore;
