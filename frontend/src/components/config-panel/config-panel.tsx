import ConfigurationForm, {
	ConfigurationFormSchema,
	type ConfigurationFormValues,
} from "@/components/config-panel/configuration-form";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from "@/components/ui/dialog";
import { Form } from "@/components/ui/form";
import { Separator } from "@/components/ui/separator";
import { agentTemplates } from "@/hooks/use-store";
import { zodResolver } from "@hookform/resolvers/zod";
import {
	AlertCircle,
	CheckCircle,
	Home,
	Play,
	Plus,
	Settings,
	Users,
	Wrench,
} from "lucide-react";
import { useFieldArray, useForm } from "react-hook-form";

const defualtValues: ConfigurationFormValues = {
	areas: [
		{
			name: "Kitchen",
			attributes: {
				temperature: 20,
			},
			agents: [
				{
					templateId: "temp_sensor" as const,
					count: 0,
				},
				{
					templateId: "motion_sensor" as const,
					count: 0,
				},
				{
					templateId: "ac_unit" as const,
					count: 0,
				},
				{
					templateId: "coffee_machine" as const,
					count: 0,
				},
				{
					templateId: "dishwasher" as const,
					count: 0,
				},
				{
					templateId: "fridge" as const,
					count: 0,
				},
				{
					templateId: "smart_light" as const,
					count: 0,
				},
			],
		},
	],
};

const ConfigPanel = () => {
	const hasConfiguration = true; // Replace with actual logic to check configuration
	const form = useForm<ConfigurationFormValues>({
		resolver: zodResolver(ConfigurationFormSchema),
		defaultValues: defualtValues,
	});

	const fieldArray = useFieldArray({
		control: form.control,
		name: "areas",
	});

	return (
		<main className="flex-1 p-4 overflow-y-auto flex flex-col justify-center">
			<div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
				{/* Configuration Panel */}
				<Card className="h-fit">
					<CardHeader>
						<CardTitle className="flex items-center">
							<Settings className="size-5 mr-2" />
							System Configuration
						</CardTitle>
						<CardDescription>
							Set up your rooms and agents before starting the
							simulation
						</CardDescription>
					</CardHeader>
					<CardContent className="flex flex-col gap-4">
						<div className="grid grid-cols-2 gap-4">
							<div className="text-center p-4 bg-blue-600/10 rounded-lg">
								<Home className="size-8 text-blue-800 dark:text-blue-600 mx-auto mb-2" />
								<div className="text-2xl font-bold text-blue-800 dark:text-blue-600">
									{/* {configuredRooms.length} */}
									Number of Rooms
								</div>
								<div className="text-sm text-blue-700">
									Rooms
								</div>
							</div>
							<div className="text-center p-4 bg-green-500/10 rounded-lg">
								<Users className="size-8 text-green-800 dark:text-green-600 mx-auto mb-2" />
								<div className="text-2xl font-bold text-green-800 dark:text-green-600">
									{/* {totalAgents} */}
									Number of Agents
								</div>
								<div className="text-sm text-green-700">
									Agents
								</div>
							</div>
						</div>

						<Separator />

						{hasConfiguration ? (
							<div className="space-y-3">
								<div className="flex items-center text-green-600">
									<CheckCircle className="h-4 w-4 mr-2" />
									<span className="font-medium">
										Configuration Ready
									</span>
								</div>
								<div className="text-sm text-gray-600">
									{/* Your smart home is configured with{" "}
									{configuredRooms.length} rooms and{" "}
									{totalAgents} agents. */}
									Your smart home is configured with
									configuredRooms.length rooms and totalAgents
									agents.
								</div>
							</div>
						) : (
							<div className="space-y-3">
								<div className="flex items-center text-amber-600">
									<AlertCircle className="h-4 w-4 mr-2" />
									<span className="font-medium">
										Configuration Required
									</span>
								</div>
								<div className="text-sm text-gray-600">
									Please configure your rooms and agents
									before starting the system.
								</div>
							</div>
						)}
						<Dialog>
							<DialogTrigger asChild>
								<Button
									variant="outline"
									className="w-full"
									// onClick={() => setShowConfiguration(true)}
								>
									<Wrench />
									{hasConfiguration
										? "Modify Configuration"
										: "Configure System"}
								</Button>
							</DialogTrigger>
							<DialogContent className="max-w-dvw max-h-dvh min-w-fit">
								<DialogHeader>
									<DialogTitle>
										<Settings className="inline mr-2" />
										System Configuration
									</DialogTitle>
									<DialogDescription>
										Configure your smart home rooms and
										agents. You can add, edit, or remove
										rooms and agents as needed.
									</DialogDescription>
								</DialogHeader>
								<Form {...form}>
									<ConfigurationForm
										fieldArray={fieldArray}
									/>
									<DialogFooter className="flex justify-between">
										<Button
											type="button"
											variant="ghost"
											size="icon"
											onClick={() =>
												fieldArray.append({
													name: "",
													attributes: {
														temperature: 21.0,
													},
													agents: agentTemplates.map(
														(template) => ({
															templateId:
																template.id,
															count: 0,
														})
													),
												})
											}
										>
											<Plus />
										</Button>
										{form.formState.isValid ? (
											<DialogClose asChild>
												<Button
													type="submit"
													form="configuration-form"
												>
													Save Configuration
												</Button>
											</DialogClose>
										) : (
											<Button
												type="submit"
												form="configuration-form"
											>
												Save Configuration
											</Button>
										)}
									</DialogFooter>
								</Form>
							</DialogContent>
						</Dialog>
					</CardContent>
				</Card>

				{/* Start System Panel */}
				<Card>
					<CardHeader>
						<CardTitle className="flex items-center">
							<Play className="h-5 w-5 mr-2" />
							System Control
						</CardTitle>
						<CardDescription>
							Start the smart home simulation with your current
							configuration
						</CardDescription>
					</CardHeader>
					<CardContent className="space-y-4">
						<div className="p-4 bg-gray-50 rounded-lg">
							<h4 className="font-medium mb-2">
								Pre-Start Checklist:
							</h4>
							<div className="space-y-2 text-sm">
								<div
									className={`flex items-center ${
										hasConfiguration
											? "text-green-600"
											: "text-gray-400"
									}`}
								>
									<CheckCircle className="h-4 w-4 mr-2" />
									Configuration complete
								</div>
								<div className="flex items-center text-green-600">
									<CheckCircle className="h-4 w-4 mr-2" />
									JADE backend ready
								</div>
								<div className="flex items-center text-green-600">
									<CheckCircle className="h-4 w-4 mr-2" />
									WebSocket connection available
								</div>
							</div>
						</div>

						<Button
							className="w-full h-12 text-lg"
							// onClick={handleStartSystem}
							// disabled={!hasConfiguration || isStarting}
						>
							{/* {isStarting ? (
								<>
									<Loader2 className="h-5 w-5 mr-2 animate-spin" />
									Starting System...
								</>
							) : (
								<>
									<Play className="h-5 w-5 mr-2" />
									Start Smart Home System
								</>
							)} */}
							<Play />
							Start Smart Home System
						</Button>

						{!hasConfiguration && (
							<p className="text-sm text-amber-600 text-center">
								Complete your configuration first to enable
								system start
							</p>
						)}
					</CardContent>
				</Card>
			</div>
			{/* Configuration Overview */}
			{hasConfiguration && (
				<Card className="mt-8">
					<CardHeader>
						<CardTitle>Configuration Overview</CardTitle>
						<CardDescription>
							Review your current smart home setup
						</CardDescription>
					</CardHeader>
					<CardContent>
						<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
							{[1, 2, 3, 4, 5].map((room) => (
								<div
									key={room}
									className="border rounded-lg p-4"
								>
									<div className="flex items-center mb-3">
										<Home className="h-5 w-5 text-blue-600 mr-2" />
										<h3 className="font-semibold">
											room.name
										</h3>
										<Badge
											variant="secondary"
											className="ml-auto"
										>
											room.agents.length agents
										</Badge>
									</div>
									<div className="space-y-2">
										<div className="flex items-center justify-between text-sm">
											<div className="flex items-center">
												{"getAgentIcon(agent.type)"}
												<span className="ml-2">
													agent.name
												</span>
											</div>
											<Badge
												variant="outline"
												// className={`text-xs ${getAgentTypeColor(
												// 	agent.type
												// )}`}
											>
												agent.type
											</Badge>
										</div>
									</div>
								</div>
							))}
						</div>
					</CardContent>
				</Card>
			)}
		</main>
	);
};
export default ConfigPanel;
